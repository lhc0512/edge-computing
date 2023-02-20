package cn.edu.scut.runner;

import cn.edu.scut.agent.MABuffer;
import cn.edu.scut.service.*;
import cn.edu.scut.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
@Lazy
public class OnlineHeuristicDataRunner implements IRunner {
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

    @Autowired
    private MABuffer buffer;

    public void run() {
        log.info("=============================");
        log.info("run heuristic-data runner");
        log.info("=============================");

        linkService.remove(null);
        edgeNodeService.remove(null);
        taskService.remove(null);
        runnerService.init();

        log.info("store communication information start!");
        runnerService.run();
        taskService.remove(null);
        log.info("store communication information end!");

        var dateFlag = DateTimeUtils.getFlag();
        var flag = dateFlag + "@" + name + "@" + this.flag;

        var episodes = new ArrayList<Double>();
        var successRates = new ArrayList<Double>();
        for (int currentEpisode = 1; currentEpisode <= episodeNumber; currentEpisode++) {
            runnerService.run();
            double successRate = taskService.getSuccessRate();
            log.info("running time: {}, success rate: {}", currentEpisode, successRate);
            episodes.add((double) currentEpisode);
            successRates.add(successRate);
            runnerService.addData();
            taskService.remove(null);
        }
        plotService.plot(episodes, successRates, flag);
        buffer.saveHdfs(flag);
        var averageSuccessRate = successRates.stream().collect(Collectors.averagingDouble(x -> x));
        log.info("average success rate: {}", averageSuccessRate);
    }
}
