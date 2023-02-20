package cn.edu.scut.service;

import cn.edu.scut.bean.*;
import cn.edu.scut.queue.ExecutionQueue;
import cn.edu.scut.queue.TransmissionQueue;
import cn.edu.scut.scheduler.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;

@Setter
@Getter
@Service
@Slf4j
@RefreshScope
public class EdgeNodeSystemService {

    @Value("${spring.application.name}")
    private String name;

    @Value("${edgeComputing.seed}")
    private Integer seed;

    @Value("${edgeComputing.cpuCapacity}")
    private Integer cpuCapacity;

    // refresh
    @Value("${edgeComputing.scheduler}")
    private String scheduler;

    @Value("${edgeComputing.minCpuCore}")
    private int minCpuCore;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EdgeNodeSystem edgeNodeSystem;

    @Lazy
    @Autowired
    private DRLScheduler DRLScheduler;

    @Lazy
    @Autowired
    private RandomScheduler randomScheduler;

    @Lazy
    @Autowired
    private ReliabilityTwoChoice reliabilityTwoChoice;

    @Lazy
    @Autowired
    private ReactiveScheduler reactiveScheduler;

    @Autowired
    private EdgeNodeService edgeNodeService;

    @Autowired
    private LinkService linkService;

    @Value("${edgeComputing.queueCoef}")
    private float queueCoef;

    @Async
    public void processUserTask(Task task) {
        String service = switch (scheduler) {
            case "rl" -> DRLScheduler.selectAction(task.getId());
            case "random" -> randomScheduler.selectAction(task.getId());
            case "reactive" -> reactiveScheduler.selectAction(task.getId());
            case "reliability-two-choice" -> reliabilityTwoChoice.selectAction(task.getId());
            default -> throw new RuntimeException("no scheduler");
        };
        if (service.equals(name)) {
            task.setBeginExecutionTime(LocalDateTime.now());
            task.setEndTransmissionTime(LocalDateTime.now());
            task.setTransmissionTime(0L);
            task.setTransmissionWaitingTime(0L);
            task.setDestination(name);
            processEdgeNodeTask(task);
        } else {
            Link link = edgeNodeSystem.getLinkMap().get(service);
            Double transmissionTime = task.getTaskSize() / link.getTransmissionRate() * 1000;
            task.setTransmissionTime(transmissionTime.longValue());
            task.setDestination(service);
            edgeNodeSystem.getTransmissionQueueMap().get(service).add(task);
        }
    }

    public void processEdgeNodeTask(Task task) {
        Double executionTime = task.getCpuCycle().doubleValue() / edgeNodeSystem.getEdgeNode().getCapacity().doubleValue() * 1000;
        task.setExecutionTime(executionTime.longValue());
        edgeNodeSystem.getExecutionQueue().add(task);
    }

    public void init() {
        var edgeNodeConfig = edgeNodeService.getOne(new QueryWrapper<EdgeNode>().eq("name", name));
        var id = Integer.parseInt(name.split("-")[2]);
        EdgeNode edgeNode = new EdgeNode();
        edgeNode.setId(id);
        edgeNode.setName(name);
        edgeNode.setCpuNum(edgeNodeConfig.getCpuNum());
        edgeNode.setExecutionFailureRate(edgeNodeConfig.getExecutionFailureRate());
        edgeNode.setTaskRate(edgeNodeConfig.getTaskRate());
        edgeNode.setCapacity(edgeNodeConfig.getCpuNum() * Constants.Giga.value * cpuCapacity);
        edgeNodeSystem.setEdgeNode(edgeNode);
        edgeNodeSystem.setExecutionQueue(new ExecutionQueue());

        var transmissionQueueMap = new HashMap<String, TransmissionQueue>();
        var linkMap = new HashMap<String, Link>();
        var links = linkService.list(new QueryWrapper<Link>().eq("source", name));
        for (Link link : links) {
            transmissionQueueMap.put(link.getDestination(), new TransmissionQueue());
            linkMap.put(link.getDestination(), link);
        }
        edgeNodeSystem.setTransmissionQueueMap(transmissionQueueMap);
        edgeNodeSystem.setLinkMap(linkMap);
        // availAction
        edgeNodeSystem.setExecutionQueueThreshold(Float.valueOf(edgeNode.getCpuNum()) / (float) minCpuCore * queueCoef);
        log.info("load edge nodes and links configuration completed");
        log.info("{} edge config: {}", name, edgeNode);
        log.info("{} link config：{}", name, links);
    }
}
