package cn.edu.scut.scheduler;


import cn.edu.scut.agent.IMAAgent;
import cn.edu.scut.bean.Task;
import cn.edu.scut.service.TaskService;
import cn.edu.scut.service.TransitionService;
import cn.edu.scut.util.ArrayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RefreshScope
public class DRLScheduler implements IScheduler {

    // heuristic method
    @Autowired(required = false)
    private IMAAgent agent;

    @Autowired
    private TransitionService transitionService;

    @Autowired
    private TaskService taskService;

    @Value("${edgeComputing.edgeNodeNumber}")
    private int agentNumber;

    @Value("${spring.application.name}")
    public String name;

    @Override
    public String selectAction(Long taskId) {
        Task task = taskService.getById(taskId);
        var availAction = new int[agentNumber + 1];
        availAction[agentNumber] = 0;
        for (int i = 1; i <= agentNumber; i++) {
            availAction[i - 1] = 1;
        }
        String s = ArrayUtils.arrayToString(availAction);
        task.setAvailAction(s);
        taskService.updateById(task);
        var state = transitionService.getState(taskId);
        int i = agent.selectAction(state, availAction, false) + 1;
        log.info("drl select action {}", i);
        return String.format("edge-node-%d", i);
    }
}