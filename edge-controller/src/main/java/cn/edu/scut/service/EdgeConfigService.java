package cn.edu.scut.service;

import cn.edu.scut.bean.*;
import cn.edu.scut.util.ArrayUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RefreshScope
public class EdgeConfigService {

    @Autowired
    private UniformRealDistribution executionFailureRandom;

    @Autowired
    private UniformIntegerDistribution cpuCoreRandom;

    @Autowired
    private UniformRealDistribution taskRateRandom;

    @Autowired
    private UniformRealDistribution transmissionRateRandom;

    @Autowired
    private UniformRealDistribution transmissionFailureRateRandom;

    @Autowired
    private UniformIntegerDistribution taskSizeRandom;

    @Autowired
    private UniformIntegerDistribution taskComplexityRandom;

    @Autowired
    private EdgeNodeService edgeNodeService;

    @Autowired
    private LinkService linkService;

    @Value("${edgeComputing.minTransmissionFailureRate}")
    Double minTransmissionFailureRate;

    @Value("${edgeComputing.maxTransmissionRate}")
    Double maxTransmissionRate;

    @Value("${edgeComputing.minTaskSize}")
    Long minTaskSize;

    @Value("${edgeComputing.maxTaskSize}")
    Long maxTaskSize;

    @Value("${edgeComputing.minTaskComplexity}")
    Long minTaskComplexity;

    @Value("${edgeComputing.maxTaskComplexity}")
    Long maxTaskComplexity;

    @Value("${edgeComputing.deadline}")
    Long deadline;

    @Value("${edgeComputing.edgeNodeNumber}")
    private int edgeNodeNumber;

    public void generateEdgeLink(String name) {
        EdgeNode edgeNode = new EdgeNode();
        edgeNode.setName(name);
        edgeNode.setExecutionFailureRate(executionFailureRandom.sample());
        // ! int -> long
        Integer core = cpuCoreRandom.sample() * 4;
        edgeNode.setCpuNum(core.longValue());
        edgeNode.setTaskRate(taskRateRandom.sample());
        edgeNodeService.save(edgeNode);

        for (int i = 1; i <= edgeNodeNumber; i++) {
            String service = String.format("edge-node-%d", i);
            Link link = new Link();
            if (service.equals(name)) {
                link.setSource(name);
                link.setDestination(name);
                link.setTransmissionRate(maxTransmissionRate * Constants.Mega.value * Constants.Byte.value);
                link.setTransmissionFailureRate(minTransmissionFailureRate);
            } else {
                link.setSource(name);
                link.setDestination(service);
                link.setTransmissionRate(transmissionRateRandom.sample() * Constants.Mega.value * Constants.Byte.value);
                link.setTransmissionFailureRate(transmissionFailureRateRandom.sample());
            }
            linkService.save(link);
        }
    }

    public Task generateTask(String name) {
        Task task = new Task();
        task.setTaskSize(((long) taskSizeRandom.sample()) * StoreConstants.Kilo.value * StoreConstants.Byte.value);
        task.setTaskComplexity((long) taskComplexityRandom.sample());
        task.setCpuCycle(task.getTaskComplexity() * task.getTaskSize());
        task.setDeadline(deadline);
        task.setSource(name);
        task.setStatus(TaskStatus.NEW);
        var availAction = new int[edgeNodeNumber + 1];
        for (int i = 0; i < edgeNodeNumber; i++) {
            availAction[i] = 1;
        }
        task.setAvailAction(ArrayUtils.arrayToString(availAction));
        return task;
    }

    public Task generateEmptyTask(String name) {
        Task task = new Task();
        task.setTaskSize(0L);
        task.setTaskComplexity(0L);
        task.setCpuCycle(0L);
        task.setDeadline(0L);
        task.setSource(name);
        task.setDestination("null");
        task.setStatus(TaskStatus.EMPTY);
        var availAction = new int[edgeNodeNumber + 1];
        availAction[edgeNodeNumber] = 1;
        task.setAvailAction(ArrayUtils.arrayToString(availAction));
        task.setExecutionTime(0L);
        task.setExecutionWaitingTime(0L);
        task.setTransmissionTime(0L);
        task.setTransmissionWaitingTime(0L);
        return task;
    }
}