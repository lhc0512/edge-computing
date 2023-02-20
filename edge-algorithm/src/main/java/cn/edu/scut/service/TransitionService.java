package cn.edu.scut.service;

import cn.edu.scut.bean.*;
import cn.edu.scut.util.ArrayUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;

@Service
@Slf4j
public class TransitionService {

    @Value("${edgeComputing.maxTaskRate}")
    private double maxTaskRate;

    @Value("${edgeComputing.maxCpuCore}")
    private int maxCpuCore;

    @Value("${edgeComputing.maxTaskSize}")
    private long maxTaskSize;

    @Value("${edgeComputing.maxTaskComplexity}")
    private long maxTaskComplexity;

    @Value("${edgeComputing.maxTransmissionRate}")
    private double maxTransmissionRate;

    @Value("${edgeComputing.maxTransmissionFailureRate}")
    private double maxTransmissionFailureRate;

    @Value("${edgeComputing.maxExecutionFailureRate}")
    private double maxExecutionFailureRate;

    @Value("${edgeComputing.deadline}")
    private int deadline;

    @Value("${edgeComputing.edgeNodeNumber}")
    private int agentNumber;

    @Autowired
    private EdgeNodeService edgeNodeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private LinkService linkService;

    public float[] getState(Long taskId) {
        var task = taskService.getById(taskId);
        var edgeNodeInfos = edgeNodeService.list();
        var linkInfos = linkService.list(new QueryWrapper<Link>().eq("source", task.getSource()));

        var obsList = new ArrayList<Float>();
        // one-hot vector N
        int id = Integer.parseInt(task.getSource().split("-")[2]) - 1;
        for (int j = 0; j < agentNumber; j++) {
            if (j == id) {
                obsList.add(1.0f);
            } else {
                obsList.add(0.0f);
            }
        }
        // task dynamic 2
        obsList.add(Float.valueOf(task.getTaskSize()) / (float) (maxTaskSize * StoreConstants.Byte.value * StoreConstants.Kilo.value));
        obsList.add(task.getTaskComplexity() / (float) maxTaskComplexity);
        // link static  N
        for (Link link : linkInfos) {
            obsList.add((float) (link.getTransmissionRate() / (maxTransmissionRate * Constants.Mega.value * Constants.Byte.value)));
        }
        // edge node static 3N
        // static information
        for (EdgeNode edgeNode : edgeNodeInfos) {
            obsList.add((float) (edgeNode.getCpuNum() / maxCpuCore));
            obsList.add((float) (edgeNode.getExecutionFailureRate() / maxExecutionFailureRate));
            obsList.add((float) (edgeNode.getTaskRate() / maxTaskRate));
        }
        return ArrayUtils.toFloatArray(obsList);
    }


    public int getAction(Long taskId) {
        var task = taskService.getById(taskId);
        int action;
        if (task.getStatus().equals(TaskStatus.EMPTY)) {
            action = agentNumber;
        } else {
            action = Integer.parseInt(task.getDestination().split("-")[2]) - 1;
        }
        return action;
    }

    public int[] getAvailAction(Long taskId) {
        var task = taskService.getById(taskId);
        return Arrays.stream(task.getAvailAction().split(",")).mapToInt(Integer::parseInt).toArray();
    }

    public float getReward(Long taskId) {
        var task = taskService.getById(taskId);
        float reward;
        if (task.getStatus().equals(TaskStatus.SUCCESS)) {
            reward = 1.0f;
        } else if (task.getStatus().equals(TaskStatus.EXECUTION_FAILURE) || task.getStatus().equals(TaskStatus.TRANSMISSION_FAILURE) || task.getStatus().equals(TaskStatus.DROP)) {
            reward = -1.0f;
        } else if (task.getStatus().equals(TaskStatus.EMPTY)) {
            return 0.0f;
        } else {
            throw new RuntimeException("task status error");
        }
        return reward;
    }
}