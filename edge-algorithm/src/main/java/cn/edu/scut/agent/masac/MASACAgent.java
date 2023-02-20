package cn.edu.scut.agent.masac;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.training.TrainingConfig;
import ai.djl.translate.NoopTranslator;
import ai.djl.translate.TranslateException;
import cn.edu.scut.agent.IMAAgent;
import cn.edu.scut.agent.MABuffer;
import cn.edu.scut.util.DJLUtils;
import cn.edu.scut.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

@Component
@Slf4j
@ConditionalOnProperty(name = "rl.name", havingValue = "masac")
public class MASACAgent implements InitializingBean, IMAAgent {

    @Value("${rl.action-shape}")
    private int actionShape;

    @Value("${rl.alpha}")
    private float alpha;

    @Value("${rl.gamma}")
    private float gamma;

    @Value("${rl.use-soft-update}")
    private boolean useSoftUpdate;

    @Value("${rl.tau}")
    private float tau;

    @Value("${rl.use-adaptive-alpha}")
    private boolean useAdaptiveAlpha;

    @Value("${rl.use-normalized-reward}")
    private boolean useNormalizedReward;

    @Autowired
    private Random schedulerRandom;

    @Autowired
    private Model q1Model;
    @Autowired
    private Model q2Model;

    @Autowired
    private Model targetQ1Model;
    @Autowired
    private Model targetQ2Model;

    @Autowired
    private Model actorModel;

    @Autowired
    private Model criticModel;

    @Autowired
    private MABuffer buffer;

    @Autowired
    private NDManager manager;

    private float targetEntropy;

    @Autowired
    private Model alphaModel;

    @Value("${spring.application.name}")
    String name;

    @Autowired
    private FileSystem fileSystem;

    @Value("${hadoop.hdfs.url}")
    private String hdfsUrl;

    @Value("${rl.use-cql}")
    private boolean useCql;

    @Value("${rl.cql-weight}")
    private float cqlWeight;

    @Value("${rl.use-addition-critic}")
    private boolean useAdditionCritic;

    @Value("${rl.target-entropy-coef}")
    private float targetEntropyCoef;

    @Autowired
    private TrainingConfig trainingConfig;

    @Override
    public void afterPropertiesSet() throws Exception {
//        TARGET_ENTROPY = (float) Math.log(1.0 / ACTION_SHAPE) * 0.98f;
        // We use 0.6 because the recommended 0.98 will cause alpha explosion.
        targetEntropy = -(float) Math.log(1.0 / actionShape) * targetEntropyCoef;
    }

    public int selectAction(float[] state, int[] availAction, boolean training) {
        int action = 0;
        var subManager = manager.newSubManager();
        try (subManager) {
            var predictor = actorModel.newPredictor(new NoopTranslator());
            try (predictor) {
                try {
                    var out = predictor.predict(new NDList(subManager.create(state))).singletonOrThrow();
                    var bool = subManager.create(availAction).eq(0);
                    out.set(bool, -1e8f);
                    var prob = out.softmax(-1);
                    action = DJLUtils.sampleMultinomial(schedulerRandom, prob);
                } catch (TranslateException e) {
                    log.error("predict error: {}", e.getMessage());
                }
            }
        }
        return action;
    }

    public void train() {
        var subManager = manager.newSubManager();
        try (subManager) {
            NDList list = buffer.sample(subManager);
            var states = list.get(0);
            var actions = list.get(1);
            var availActions = list.get(2);
            var rewards = list.get(3);
            var nextStates = list.get(4);

            if (useNormalizedReward) {
                var mean = rewards.mean();
                var std = rewards.sub(mean).pow(2).mean().sqrt().add(1e-5f);
                rewards = rewards.sub(mean).div(std);
            }

            var q1Trainer = q1Model.newTrainer(trainingConfig);
            var q2Trainer = q2Model.newTrainer(trainingConfig);
            var actorTrainer = actorModel.newTrainer(trainingConfig);
            var criticTrainer = criticModel.newTrainer(trainingConfig);
            var targetQ1Predictor = targetQ1Model.newPredictor(new NoopTranslator());
            var targetQ2Predictor = targetQ2Model.newPredictor(new NoopTranslator());
            var alphaTrainer = alphaModel.newTrainer(trainingConfig);
            NDArray alphaValue;
            if (useAdaptiveAlpha) {
                alphaValue = alphaModel.getBlock().getParameters().get("alpha").getArray().duplicate().exp();
            } else {
                alphaValue = subManager.create(alpha);
            }
            try (q1Trainer; q2Trainer; actorTrainer; criticTrainer; targetQ1Predictor; targetQ2Predictor; alphaTrainer) {
                var actorOut = actorTrainer.evaluate(new NDList(nextStates)).singletonOrThrow();
                var nextLogProbabilities = actorOut.logSoftmax(-1);
                NDArray nextTargetQ1;
                NDArray nextTargetQ2;
                try {
                    nextTargetQ1 = targetQ1Predictor.predict(new NDList(nextStates)).singletonOrThrow();
                    nextTargetQ2 = targetQ2Predictor.predict(new NDList(nextStates)).singletonOrThrow();
                } catch (TranslateException e) {
                    throw new RuntimeException(e);
                }
                var targetQ = nextLogProbabilities.exp().mul(NDArrays.minimum(nextTargetQ1, nextTargetQ2).sub(nextLogProbabilities.mul(alphaValue)));
                var avgTargetQ = targetQ.sum(new int[]{-1}, true);
                var target = rewards.add(avgTargetQ.mul(gamma));
                var q1Value = q1Trainer.evaluate(new NDList(states)).singletonOrThrow();
                var q2Value = q2Trainer.evaluate(new NDList(states)).singletonOrThrow();
                var lobProbabilityValue = actorTrainer.evaluate(new NDList(states)).singletonOrThrow().logSoftmax(-1);

                if (useAdaptiveAlpha) {
                    var alphaGradientCollector = alphaTrainer.newGradientCollector();
                    try (alphaGradientCollector) {
                        var entropy = lobProbabilityValue.exp().mul(lobProbabilityValue).sum(new int[]{-1}).mean().neg();
                        var logAlpha = alphaModel.getBlock().getParameters().get("alpha").getArray();
                        var loss = logAlpha.mul(entropy.sub(targetEntropy));
                        alphaGradientCollector.backward(loss);
                        alphaTrainer.step();
                    }
                }

                var actorGradientCollector = actorTrainer.newGradientCollector();
                try (actorGradientCollector) {
                    var qMin = NDArrays.minimum(q1Value, q2Value);
                    var lobProbabilities = actorTrainer.forward(new NDList(states)).singletonOrThrow().logSoftmax(-1);
                    var loss = lobProbabilities.exp().mul(qMin.sub(lobProbabilities.mul(alphaValue))).sum(new int[]{-1}).mean().neg();
                    actorGradientCollector.backward(loss);
                    actorTrainer.step();
                }

                var q1GradientCollector = q1Trainer.newGradientCollector();
                try (q1GradientCollector) {
                    var q1 = q1Trainer.forward(new NDList(states)).singletonOrThrow();
                    var q1Action = q1.gather(actions, -1);
                    var loss1 = q1Action.sub(target).pow(2).mean();
                    if (useCql) {
                        var cqlLoss1 = (q1.exp().sum().log().mean()).sub(q1Action.mean());
                        loss1.add(cqlLoss1.mul(cqlWeight));
                    }
                    q1GradientCollector.backward(loss1);
                    q1Trainer.step();
                }
                var q2GradientCollector = q2Trainer.newGradientCollector();
                try (q2GradientCollector) {
                    var q2 = q2Trainer.forward(new NDList(states)).singletonOrThrow();
                    var q2Action = q2.gather(actions, -1);
                    var loss2 = q2Action.sub(target).pow(2).mean();
                    if (useCql) {
                        var cqlLoss2 = (q2.exp().sum().log().mean()).sub(q2Action.mean());
                        loss2.add(cqlLoss2.mul(cqlWeight));
                    }
                    q2GradientCollector.backward(loss2);
                    q2Trainer.step();
                }
                if (useAdditionCritic) {
                    var criticGradientCollector = criticTrainer.newGradientCollector();
                    try (criticGradientCollector) {
                        var value = criticTrainer.evaluate(new NDList(states)).singletonOrThrow();
                        var loss = value.sub(target).pow(2).mean();
                        criticGradientCollector.backward(loss);
                        criticTrainer.step();
                    }
                }
            }
            if (useSoftUpdate) {
                DJLUtils.softUpdate(q1Model.getBlock(), targetQ1Model.getBlock(), tau);
                DJLUtils.softUpdate(q1Model.getBlock(), targetQ2Model.getBlock(), tau);
            }
        }
    }

    public void saveModel(String path) {
        String basePath = "results/model/" + path + "/";
        var actorPath = basePath + "actor.param";
        var criticPath = basePath + "critic.param";
        try {
            Files.createDirectories(Paths.get(actorPath).getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DJLUtils.saveModel(Paths.get(actorPath), actorModel.getBlock());
        DJLUtils.saveModel(Paths.get(criticPath), criticModel.getBlock());
    }

    public void loadModel(String path) {
        String basePath = "results/model/" + path + "/";
        var actorPath = basePath + "actor.param";
        var criticPath = basePath + "critic.param";
        DJLUtils.loadModel(Paths.get(actorPath), actorModel.getBlock(), manager);
        DJLUtils.loadModel(Paths.get(criticPath), criticModel.getBlock(), manager);
    }

    // edge-experiment
    @Override
    public void saveHdfsModel(String flag) {
        String basePath = "results/model/" + flag;
        try {
            actorModel.save(Paths.get(basePath), null);
            criticModel.save(Paths.get(basePath), null);
        } catch (IOException e) {
            log.error("save model error: {}", e.getMessage());
        }
        var actorPath = basePath + "/actor-0000.params";
        var criticPath = basePath + "/critic-0000.params";
        try {
            if (fileSystem.exists(new Path(hdfsUrl + "/" + basePath))) {
                fileSystem.delete(new Path(hdfsUrl + "/" + basePath), true);
            }
        } catch (IOException e) {
            log.error("delete file in file system error: {}", e.getMessage());
        }
        try {
            fileSystem.copyFromLocalFile(true, true, new Path(actorPath), new Path(hdfsUrl + "/" + actorPath));
            fileSystem.copyFromLocalFile(true, true, new Path(criticPath), new Path(hdfsUrl + "/" + criticPath));
        } catch (IOException e) {
            log.error("file system save file error: {}", e.getMessage());
        }
    }

    // edge-node, edge-experiment
    @Override
    public void loadHdfsModel(String flag) {
        String basePath = "results/model/" + flag;
        try {
            var actorPath = basePath + "/actor-0000.params";
            var criticPath = basePath + "/critic-0000.params";
            fileSystem.copyToLocalFile(false, new Path(hdfsUrl + "/" + actorPath), new Path(actorPath), true);
            fileSystem.copyToLocalFile(false, new Path(hdfsUrl + "/" + criticPath), new Path(criticPath), true);
        } catch (IOException e) {
            log.info("load hdfs model: {}", e.getMessage());
        }
        try {
            actorModel.load(Paths.get(basePath));
            actorModel.load(Paths.get(basePath));
        } catch (IOException | MalformedModelException e) {
            throw new RuntimeException(e);
        }
        FileUtils.recursiveDelete(Paths.get(basePath).toFile());
    }

    @Override
    public void loadSteamModel(InputStream inputStream, String fileName) {
        switch (fileName) {
            case "actor.param" -> {
                try {
                    actorModel.load(inputStream);
                } catch (IOException | MalformedModelException e) {
                    log.info("load stream model error: {}", e.getMessage());
                }
            }
            case "critic.param" -> {
                try {
                    criticModel.load(inputStream);
                } catch (IOException | MalformedModelException e) {
                    log.info("load stream model error: {}", e.getMessage());
                }
            }
            default -> throw new RuntimeException("fileName error!");
        }
    }
}
