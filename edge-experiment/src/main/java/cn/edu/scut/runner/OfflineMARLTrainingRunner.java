package cn.edu.scut.runner;

import cn.edu.scut.agent.IMAAgent;
import cn.edu.scut.agent.MABuffer;
import cn.edu.scut.service.*;
import cn.edu.scut.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Lazy
@Service
@Slf4j
public class OfflineMARLTrainingRunner implements IRunner {

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

    @Autowired
    private MABuffer buffer;

    @Value("${rl.test-frequency}")
    private int testFrequency;

    @Value("${rl.training-time}")
    private int trainingTime;

    @Value("${rl.buffer-path}")
    private String bufferPath;

    @Value("${edgeComputing.flag}")
    private String flag;

    public void run() {
        log.info("========================");
        log.info("run rl-offline runner.");
        log.info("========================");

        // remove data
        linkService.remove(null);
        edgeNodeService.remove(null);
        taskService.remove(null);
        // init edge node and link
        runnerService.init();
        buffer.loadHdfs(bufferPath);


        log.info("store communication information start!");
        runnerService.run();
        taskService.remove(null);
        log.info("store communication information end!");

        var dateFlag = DateTimeUtils.getFlag();
        var flag = dateFlag + "@" + rlName + "@" + this.flag;

        var episodes = new ArrayList<Double>();
        var successRates = new ArrayList<Double>();
        for (int i = 0; i <= trainingTime; i++) {
            if (i % testFrequency == 0) {
                // update model
                agent.saveHdfsModel(flag);
                runnerService.updateModelByHdfs(flag);
                // performance
                double successRate = runnerService.test();
                log.info("train time: {}, test success rate: {}", i, successRate);
                episodes.add((double) i);
                successRates.add(successRate);
            }
            agent.train();
        }
        plotService.plot(episodes, successRates, flag);
    }
}
