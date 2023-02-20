package cn.edu.scut.runner;

import cn.edu.scut.service.EdgeNodeService;
import cn.edu.scut.service.LinkService;
import cn.edu.scut.service.RunnerService;
import cn.edu.scut.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


@Service
@Lazy
@Slf4j
public class OnlineMARLTestingRunner implements IRunner {

    @Autowired
    private TaskService taskService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private EdgeNodeService edgeNodeService;

    @Autowired
    private RunnerService runnerService;

    @Value("${rl.model-flag}")
    private String modelFlag;

    @Override
    public void run() {
        log.info("========================");
        log.info("run rl-test runner!");
        log.info("========================");

        // remove data
        linkService.remove(null);
        edgeNodeService.remove(null);
        taskService.remove(null);
        // init edge node and link
        runnerService.init();
        // update model
        runnerService.updateModelByHdfs(modelFlag);

        log.info("store communication information start!");
        runnerService.run();
        taskService.remove(null);
        log.info("store communication information end!");

        runnerService.test();
    }
}
