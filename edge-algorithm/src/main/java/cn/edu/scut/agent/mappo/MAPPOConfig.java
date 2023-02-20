package cn.edu.scut.agent.mappo;

import ai.djl.Model;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Block;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.TrainingConfig;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.PolynomialDecayTracker;
import ai.djl.training.tracker.Tracker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rl.name", havingValue = "mappo")
public class MAPPOConfig {
    @Value("${rl.epoch}")
    private int epoch;

    @Value("${rl.start-learning-rate}")
    private float startLearningRate;

    @Value("${rl.end-learning-rate}")
    private float endLearningRate;

    @Value("${rl.use-learning-rate-decay}")
    private boolean useLearningRateDecay;

    @Value("${rl.learning-rate}")
    private float learningRate;

    @Value("${rl.use-clip-grad}")
    private boolean useClipGrad;

    @Value("${rl.clip-grad-coef}")
    private float clipGradCoef;

    @Value("${rl.hidden-shape}")
    private int hiddenShape;

    @Value("${rl.action-shape}")
    private int actionShape;

    @Value("${rl.state-shape}")
    private int stateShape;

    @Value("${edgeComputing.episodeNumber}")
    private int episodeNumber;

    @Value("${rl.batch-size}")
    private int batchSize;

    @Value("${edgeComputing.edgeNodeNumber}")
    private int agentNumber;

    @Bean
    public Tracker polynomialDecayTracker() {
        int trainNum = episodeNumber * epoch * 2;
        return PolynomialDecayTracker.builder()
                .setBaseValue(startLearningRate)
                .setEndLearningRate(endLearningRate)
                .setDecaySteps(trainNum)  // share by actor and critic
                .build();
    }

    @Bean
    public Optimizer optimizer(Tracker polynomialDecayTracker) {
        var adam = Optimizer.adam();
        if (useLearningRateDecay) {
            adam.optLearningRateTracker(polynomialDecayTracker);
        } else {
            adam.optLearningRateTracker(Tracker.fixed(learningRate));
        }
        if (useClipGrad) {
            adam.optClipGrad(clipGradCoef);
        }
        return adam.build();
    }

    public Block createNetwork(NDManager manager, int outputDim) {
        var block = new SequentialBlock();
        block.add(Linear.builder().setUnits(hiddenShape).build());
        block.add(Activation::relu);
        block.add(Linear.builder().setUnits(hiddenShape).build());
        block.add(Activation::relu);
        block.add(Linear.builder().setUnits(outputDim).build());
        block.initialize(manager, DataType.FLOAT32, new Shape(batchSize, agentNumber, stateShape));
        return block;
    }

    @Bean
    public Model actorModel(NDManager manager) {
        var model = Model.newInstance("actor");
        model.setBlock(createNetwork(manager, actionShape));
        return model;
    }

    @Bean
    public Model criticModel(NDManager manager) {
        var model = Model.newInstance("critic");
        model.setBlock(createNetwork(manager, 1));
        return model;
    }

    @Bean
    public Loss loss() {
        return new Loss("null") {
            @Override
            public NDArray evaluate(NDList ndList, NDList ndList1) {
                return null;
            }
        };
    }

    @Bean
    public TrainingConfig trainingConfig(Optimizer optimizer, Loss loss) {
        return new DefaultTrainingConfig(loss)
                .optOptimizer(optimizer);
    }
}
