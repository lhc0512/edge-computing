package cn.edu.scut.thread;

import cn.edu.scut.bean.EdgeNodeSystem;
import cn.edu.scut.bean.Task;
import cn.edu.scut.bean.TaskStatus;
import cn.edu.scut.service.TaskService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Component
@Setter
@Scope("prototype")
@RefreshScope
public class ExecutionRunnable implements Runnable {

    // prototype
    @Getter
    private Task task;
    @Autowired
    private TaskService taskService;

    @Autowired
    private EdgeNodeSystem edgeNodeSystem;

    @Autowired
    private Random reliabilityRandom;

    @Override
    public void run() {
        task.setBeginExecutionTime(LocalDateTime.now());
        task.setExecutionWaitingTime(Duration.between(task.getEndTransmissionTime(), task.getBeginExecutionTime()).toMillis());
        // drop the task without wasting the resource.
        long estimatedTotalTime = task.getTransmissionWaitingTime() + task.getTransmissionTime() + task.getExecutionWaitingTime() + task.getExecutionTime();
        if (estimatedTotalTime > task.getDeadline()) {
            task.setStatus(TaskStatus.DROP);
            taskService.updateById(task);
            return;
        }
        try {
            Thread.sleep(task.getExecutionTime());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        task.setEndExecutionTime(LocalDateTime.now());
        // reliability
        double reliability = Math.exp(-task.getExecutionTime() / 1000.0 * edgeNodeSystem.getEdgeNode().getExecutionFailureRate());
        if (reliabilityRandom.nextDouble() > reliability) {
            task.setStatus(TaskStatus.EXECUTION_FAILURE);
        }
        else {
            task.setStatus(TaskStatus.SUCCESS);
        }
        taskService.updateById(task);
    }
}
