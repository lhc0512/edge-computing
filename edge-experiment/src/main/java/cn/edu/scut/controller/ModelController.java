package cn.edu.scut.controller;

import cn.edu.scut.agent.IMAAgent;
import cn.edu.scut.service.TransitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ModelController {


    @Autowired(required = false)
    private IMAAgent agent;

    @Autowired
    private TransitionService transitionService;

    @GetMapping("/action/{taskId}")
    public Integer action(@PathVariable("taskId") Long taskId) {
        var availAction = transitionService.getAvailAction(taskId);
        var state = transitionService.getState(taskId);
        int i = agent.selectAction(state, availAction, false) + 1;
        return i;
    }
}
