package cn.edu.scut.runner;

import cn.edu.scut.agent.IMAAgent;
import cn.edu.scut.service.*;
import cn.edu.scut.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * online
 * offline-to-online
 */
@Component
@Slf4j
@Lazy
public class OnlineMARLTrainingRunner implements IRunner {

    @Value("${edgeComputing.episodeNumber}")
    private int episodeNumber;

    @Value("${rl.name}")
    private String rlName;

    @Autowired
    private IMAAgent agent;

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

    @Value("${rl.use-trained-model}")
    private boolean useTrainedModel;

    @Value("${rl.model-flag}")
    private String modelFlag;

    @Value("${edgeComputing.flag}")
    private String flag;

    @Value("${edgeComputing.testFrequency}")
    private int testFrequency;

    public void run() {
        log.info("========================");
        log.info("run rl-online runner!");
        log.info("========================");
        // remove data
        linkService.remove(null);
        edgeNodeService.remove(null);
        taskService.remove(null);
        // init edge node and link
        runnerService.init();

        // offline to online
        if (useTrainedModel) {
            agent.loadHdfsModel(modelFlag);
            log.info("load model from hdfs.");
        }
        var dateFlag = DateTimeUtils.getFlag();
        var flag = dateFlag + "@" + rlName + "@" + this.flag;

        log.info("store communication information start!");
        runnerService.run();
        taskService.remove(null);
        log.info("store communication information end!");

        var episodes = new ArrayList<Double>();
        var successRates = new ArrayList<Double>();
        for (int currentEpisode = 0; currentEpisode <= episodeNumber; currentEpisode++) {
            // update model
            agent.saveHdfsModel(flag);
            runnerService.updateModelByHdfs(flag);
            // performance
            if (currentEpisode % testFrequency == 0) {
                log.info("start to test.");
                var successRate = runnerService.test();
                episodes.add((double) currentEpisode);
                successRates.add(successRate);
                log.info("end test.");
            }
            // run episode
            runnerService.run();
            var successRate = taskService.getSuccessRate();
            log.info("training episode {}, success rate {}", currentEpisode, successRate);
            // process data
            runnerService.addData();
            // training
            agent.train();
            // remove data
            taskService.remove(null);
        }
        agent.saveHdfsModel(flag);
        // plot
        plotService.plot(episodes, successRates, flag);
    }
}
