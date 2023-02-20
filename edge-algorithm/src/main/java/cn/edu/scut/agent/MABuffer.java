package cn.edu.scut.agent;


import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import cn.edu.scut.util.FileUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

@Component
@Lazy
@Slf4j
public class MABuffer implements InitializingBean {

    @Value("${rl.state-shape}")
    private int stateShape;

    @Value("${edgeComputing.edgeNodeNumber}")
    private int agentNumber;

    @Value("${rl.action-shape}")
    private int actionShape;

    @Value("${rl.buffer-size}")
    private int bufferSize;

    @Value("${rl.batch-size}")
    private int batchSize;

    @Value("${hadoop.hdfs.url}")
    private String hdfsUrl;

    @Getter
    private float[][][] states;
    @Getter
    private int[][][] actions;
    @Getter
    private float[][][] rewards;
    @Getter
    private int[][][] availActions;
    @Getter
    private float[][][] nextStates;

    private int index = 0;

    private int size = 0;

    @Autowired
    private FileSystem fileSystem;

    @Autowired
    private Random bufferRandom;

    @Override
    public void afterPropertiesSet() {
        log.info("buffer-size {} ", bufferSize);
        states = new float[bufferSize][agentNumber][stateShape];
        actions = new int[bufferSize][agentNumber][1];
        availActions = new int[bufferSize][agentNumber][actionShape];
        rewards = new float[bufferSize][agentNumber][1];
        nextStates = new float[bufferSize][agentNumber][stateShape];
    }

    public void insert(float[][] state, int[][] action, int[][] availAction, float[][] reward, float[][] nextState) {
        states[index] = state;
        actions[index] = action;
        availActions[index] = availAction;
        rewards[index] = reward;
        nextStates[index] = nextState;
        index = (index + 1) % bufferSize;
        size = Math.min(size + 1, bufferSize);
    }

    // off-policy
    public NDList sample(NDManager manager) {
        var list = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        Collections.shuffle(list, bufferRandom);
        var batch = new int[batchSize];
        for (int i = 0; i < batchSize; i++) {
            batch[i] = list.get(i);
        }

        var ndStates = manager.zeros(new Shape(batchSize, agentNumber, stateShape), DataType.FLOAT32);
        var ndActions = manager.zeros(new Shape(batchSize, agentNumber, 1), DataType.INT32);
        var ndAvailActions = manager.zeros(new Shape(batchSize, agentNumber, actionShape), DataType.INT32);
        var ndRewards = manager.zeros(new Shape(batchSize, agentNumber, 1), DataType.FLOAT32);
        var ndNextStates = manager.zeros(new Shape(batchSize, agentNumber, stateShape), DataType.FLOAT32);

        for (int i = 0; i < batchSize; i++) {
            var ndIndex = new NDIndex(i);
            ndStates.set(ndIndex, manager.create(states[batch[i]]));
            ndActions.set(ndIndex, manager.create(actions[batch[i]]));
            ndAvailActions.set(ndIndex, manager.create(availActions[batch[i]]));
            ndRewards.set(ndIndex, manager.create(rewards[batch[i]]));
            ndNextStates.set(ndIndex, manager.create(nextStates[batch[i]]));
        }
        return new NDList(ndStates, ndActions, ndAvailActions, ndRewards, ndNextStates);
    }

    // on-policy
    public NDList sampleAll(NDManager manager) {
        var ndStates = manager.zeros(new Shape(bufferSize, agentNumber, stateShape), DataType.FLOAT32);
        var ndActions = manager.zeros(new Shape(bufferSize, agentNumber, 1), DataType.INT32);
        var ndAvailActions = manager.zeros(new Shape(bufferSize, agentNumber, actionShape), DataType.INT32);
        var ndRewards = manager.zeros(new Shape(bufferSize, agentNumber, 1), DataType.FLOAT32);
        var ndNextStates = manager.zeros(new Shape(bufferSize, agentNumber, stateShape), DataType.FLOAT32);
        for (int i = 0; i < bufferSize; i++) {
            var ndIndex = new NDIndex(i);
            ndStates.set(ndIndex, manager.create(states[i]));
            ndActions.set(ndIndex, manager.create(actions[i]));
            ndAvailActions.set(ndIndex, manager.create(availActions[i]));
            ndRewards.set(ndIndex, manager.create(rewards[i]));
            ndNextStates.set(ndIndex, manager.create(nextStates[i]));
        }
        return new NDList(ndStates, ndActions, ndAvailActions, ndRewards, ndNextStates);
    }

    public void saveHdfs(String flag) {
        var path = Paths.get("results", "buffer", flag);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String basePath = "results/buffer/" + flag + "/";
        try {
            FileUtils.writeObject(states, Paths.get("results", "buffer", flag, "states.array"));
            fileSystem.copyFromLocalFile(true, true, new Path(basePath + "states.array"), new Path(hdfsUrl + "/" + basePath + "states.array"));

            FileUtils.writeObject(actions, Paths.get("results", "buffer", flag, "actions.array"));
            fileSystem.copyFromLocalFile(true, true, new Path(basePath + "actions.array"), new Path(hdfsUrl + "/" + basePath + "actions.array"));

            FileUtils.writeObject(availActions, Paths.get("results", "buffer", flag, "availActions.array"));
            fileSystem.copyFromLocalFile(true, true, new Path(basePath + "availActions.array"), new Path(hdfsUrl + "/" + basePath + "availActions.array"));

            FileUtils.writeObject(rewards, Paths.get("results", "buffer", flag, "rewards.array"));
            fileSystem.copyFromLocalFile(true, true, new Path(basePath + "rewards.array"), new Path(hdfsUrl + "/" + basePath + "rewards.array"));

            FileUtils.writeObject(nextStates, Paths.get("results", "buffer", flag, "nextStates.array"));
            fileSystem.copyFromLocalFile(true, true, new Path(basePath + "nextStates.array"), new Path(hdfsUrl + "/" + basePath + "nextStates.array"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadHdfs(String bufferPath) {
        var path = Paths.get("results", "buffer", bufferPath);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String basePath = "results/buffer/" + bufferPath + "/";
        try {
            fileSystem.copyToLocalFile(false, new Path(hdfsUrl + "/" + basePath + "states.array"), new Path(basePath + "states.array"), false);
            states = (float[][][]) FileUtils.readObject(Paths.get("results", "buffer", bufferPath, "states.array"));

            fileSystem.copyToLocalFile(false, new Path(hdfsUrl + "/" + basePath + "actions.array"), new Path(basePath + "actions.array"), false);
            actions = (int[][][]) FileUtils.readObject(Paths.get("results", "buffer", bufferPath, "actions.array"));

            fileSystem.copyToLocalFile(false, new Path(hdfsUrl + "/" + basePath + "availActions.array"), new Path(basePath + "availActions.array"), false);
            availActions = (int[][][]) FileUtils.readObject(Paths.get("results", "buffer", bufferPath, "availActions.array"));

            fileSystem.copyToLocalFile(false, new Path(hdfsUrl + "/" + basePath + "rewards.array"), new Path(basePath + "rewards.array"), false);
            rewards = (float[][][]) FileUtils.readObject(Paths.get("results", "buffer", bufferPath, "rewards.array"));

            fileSystem.copyToLocalFile(false, new Path(hdfsUrl + "/" + basePath + "nextStates.array"), new Path(basePath + "nextStates.array"), false);
            nextStates = (float[][][]) FileUtils.readObject(Paths.get("results", "buffer", bufferPath, "nextStates.array"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        index = bufferSize;
        size = bufferSize;
    }
}
