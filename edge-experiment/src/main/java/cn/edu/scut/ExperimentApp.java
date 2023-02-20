package cn.edu.scut;

import cn.edu.scut.runner.*;
import cn.edu.scut.util.SpringBeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExperimentApp {
    public static void main(String[] args) {
        var context = SpringApplication.run(ExperimentApp.class, args);
        SpringBeanUtils.setApplicationContext(context);
        var env = context.getEnvironment();
        var runnerType = env.getProperty("edgeComputing.runner", String.class);
        assert runnerType != null;
        var runner = switch (runnerType) {
            case "rl-online" -> context.getBean(OnlineMARLTrainingRunner.class);
            case "rl-offline" -> context.getBean(OfflineMARLTrainingRunner.class);
            case "rl-test" -> context.getBean(OnlineMARLTestingRunner.class);
            case "heuristic" -> context.getBean(OnlineHeuristicTestRunner.class);
            case "heuristic-data" -> context.getBean(OnlineHeuristicDataRunner.class);
            default -> throw new RuntimeException("error in runner type.");
        };
        // waiting for edge-nodes and edge-controller start.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        runner.run();
    }
}