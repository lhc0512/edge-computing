package cn.edu.scut.runner;

import cn.edu.scut.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Lazy
public class OnlineHeuristicTestRunner implements IRunner {
    @Autowired
    private TaskService taskService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private EdgeNodeService edgeNodeService;

    @Autowired
    private PlotService plotService;

    @Autowired
    private RunnerService runnerService;

    @Value("${edgeComputing.episodeNumber}")
    private int episodeNumber;

    @Value("${heuristic.name}")
    private String name;

    @Value("${edgeComputing.flag}")
    private String flag;

    @Value("${edgeComputing.taskSeed}")
    private int taskSeed;

    public void run() {
        log.info(" task seed : {}", taskSeed);
        log.info("=============================");
        log.info("run heuristic runner.");
        log.info("=============================");
        linkService.remove(null);
        edgeNodeService.remove(null);
        taskService.remove(null);
        runnerService.init();

        log.info("store communication information start!");
        runnerService.run();
        taskService.remove(null);
        log.info("store communication information end!");

        runnerService.test();
    }
}
