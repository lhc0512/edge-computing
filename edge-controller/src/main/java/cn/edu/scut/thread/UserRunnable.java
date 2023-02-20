package cn.edu.scut.thread;

import cn.edu.scut.bean.EdgeNode;
import cn.edu.scut.service.EdgeConfigService;
import cn.edu.scut.service.EdgeNodeService;
import cn.edu.scut.service.TaskService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.RandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Setter
@Slf4j
@Scope("prototype")
@Component
public class UserRunnable implements Runnable {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RandomGenerator taskRandomGenerator;

    @Autowired
    private EdgeConfigService edgeConfigService;

    @Autowired
    private EdgeNodeService edgeNodeService;

    @Autowired
    private TaskService taskService;

    private int timeSlot = 0;

    @Value("${edgeComputing.episodeLimit}")
    private int episodeLimit;

    @Override
    public void run() {
        timeSlot += 1;
        if (timeSlot > episodeLimit + 1) {
            return;
        }
        try {
            List<EdgeNode> edgeNodes = edgeNodeService.list();
            for (EdgeNode edgeNode : edgeNodes) {
                boolean flag = taskRandomGenerator.nextDouble() < edgeNode.getTaskRate();
                if (flag) {
                    var task = edgeConfigService.generateTask(edgeNode.getName());
                    task.setTimeSlot(timeSlot);
                    taskService.save(task);
                    String url = String.format("http://%s/user/task", edgeNode.getName());
                    restTemplate.postForObject(url, task, String.class);
                } else {
                    var task = edgeConfigService.generateEmptyTask(edgeNode.getName());
                    task.setTimeSlot(timeSlot);
                    taskService.save(task);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}