package cn.edu.scut.service;


import cn.edu.scut.agent.MABuffer;
import cn.edu.scut.bean.Task;
import cn.edu.scut.util.MathUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Map;

@Service
@Slf4j
public class RunnerService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TaskService taskService;

    @Value("${edgeComputing.edgeNodeNumber}")
    private int agentNumber;

    @Value("${edgeComputing.episodeLimit}")
    private int episodeLimit;

    @Value("${edgeComputing.timeSlot}")
    private int timeSlotLen;

    // heuristic-test
    @Value("${rl.state-shape:0}")
    private int stateShape;

    @Value("${edgeComputing.testNumber}")
    private int testNumber;

    // heuristic-test
    @Lazy
    @Autowired(required = false)
    private MABuffer buffer;

    @Autowired
    private TransitionService transitionService;

    public void init() {
        var controllerUrl = "http://edge-controller";
        var generateMessage = restTemplate.getForObject(controllerUrl + "/generate", String.class);
        log.info("generate edge nodes configuration: {}", generateMessage);
        var initMessage = restTemplate.getForObject(controllerUrl + "/init", String.class);
        log.info("init edge nodes: {}", initMessage);
    }

    public void run() {
        var controllerUrl = "http://edge-controller";
        var restartMessage = restTemplate.getForObject(controllerUrl + "/restart", String.class);
        log.info("restart experiment : {}", restartMessage);
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("{}", e.getMessage());
            }
            var count = taskService.count(new QueryWrapper<Task>().ne("status", "NEW"));
            if (count >= (episodeLimit + 1) * agentNumber) {
                break;
            }
        }
        var stopMessage = restTemplate.getForObject(controllerUrl + "/stop", String.class);
        log.info("stop experiment: {}", stopMessage);
    }

    public double test() {
        var list = new ArrayList<Double>();
        for (int i = 1; i <= testNumber; i++) {
            run();
            var successRate = taskService.getSuccessRate();
            log.info("test {}, success rate : {}", i, successRate);
            list.add(successRate);
            taskService.remove(null);
        }
        var avg = MathUtils.avg(list);
        var std = MathUtils.std(list);
        log.info("avg success rate: {}", avg);
        log.info("std success rate: {}", std);
        return avg;
    }

    public void updateModelByHdfs(String flag) {
        for (int i = 1; i < agentNumber; i++) {
            String edgeNodeId = String.format("edge-node-%d", i);
            restTemplate.getForObject("http://" + edgeNodeId + "/updateParam/{flag}", String.class, Map.of("flag", flag));
        }
    }

    public void updateModelByFileTransfer(String flag) {
        for (int i = 1; i < agentNumber; i++) {
            String edgeNodeId = String.format("edge-node-%d", i);
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            var multipartBodyBuilder = new MultipartBodyBuilder();
            var file1 = new FileSystemResource("results/model/" + flag + "/actor.param");
            multipartBodyBuilder.part("actor", file1, MediaType.TEXT_PLAIN);
            var multipartBody = multipartBodyBuilder.build();
            var httpEntity = new HttpEntity<>(multipartBody, headers);
            try {
                restTemplate.postForObject("http://" + edgeNodeId + "/updateModel", httpEntity, String.class);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public void addData() {
        var teamRewards = new float[episodeLimit * 2];
        var states = new float[episodeLimit][agentNumber][stateShape];
        var actions = new int[episodeLimit][agentNumber][1];
        var availActions = new int[episodeLimit][agentNumber][1];
        var rewards = new float[episodeLimit][agentNumber][1];
        var nextStates = new float[episodeLimit][agentNumber][stateShape];

        for (int i = 1; i <= episodeLimit; i++) {
            for (int j = 1; j <= agentNumber; j++) {
                var source = String.format("edge-node-%d", j);
                var task = taskService.getOne(new QueryWrapper<Task>().eq("source", source).eq("time_slot", i));
                var nextTask = taskService.getOne(new QueryWrapper<Task>().eq("source", source).eq("time_slot", i + 1));
                var state = transitionService.getState(task.getId());
                states[i - 1][j - 1] = state;
                var action = transitionService.getAction(task.getId());
                actions[i - 1][j - 1] = new int[]{action};
                var nextState = transitionService.getState(nextTask.getId());
                nextStates[i - 1][j - 1] = nextState;
                var availAction = transitionService.getAvailAction(task.getId());
                availActions[i - 1][j - 1] = availAction;
                var reward = transitionService.getReward(task.getId());
                long totalTime = task.getTransmissionWaitingTime() + task.getTransmissionTime() + task.getExecutionWaitingTime() + task.getExecutionTime();
                var endTimeSlot = task.getTimeSlot() + totalTime / timeSlotLen;
                int timeSlotIndex = (int) endTimeSlot - 1;
                teamRewards[timeSlotIndex] += reward;
            }
        }
        for (int i = 1; i <= episodeLimit; i++) {
            for (int j = 1; j <= agentNumber; j++) {
                rewards[i - 1][j - 1] = new float[]{teamRewards[i - 1]};
            }
        }
        for (int i = 1; i <= episodeLimit; i++) {
            buffer.insert(states[i - 1], actions[i - 1], availActions[i - 1], rewards[i - 1], nextStates[i - 1]);
        }
    }
}
