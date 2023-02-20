package cn.edu.scut.controller;

import cn.edu.scut.service.EdgeConfigService;
import cn.edu.scut.service.UserService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@CommonsLog
public class EdgeNodeController {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EdgeConfigService edgeConfigService;

    @Autowired
    private UserService userService;

    @Value("${edgeComputing.edgeNodeNumber}")
    private int edgeNodeNumber;

    @GetMapping("/generate")
    public String generate() {
        log.info("generate all edge node configuration.");
        for (int i = 1; i <= edgeNodeNumber; i++) {
            String name = String.format("edge-node-%d", i);
            edgeConfigService.generateEdgeLink(name);
        }
        return "success\n";
    }

    @GetMapping("/init")
    public String init() {
        log.info("init all edge node configuration.");
        for (int i = 1; i <= edgeNodeNumber; i++) {
            String name = String.format("edge-node-%d", i);
            log.info("init " + name);
            String url = String.format("http://%s/cmd/init", name);
            restTemplate.getForObject(url, String.class);
        }
        return "success\n";
    }

    @GetMapping("/start")
    public String start() {
        log.info("start experiment.");
        userService.start();
        return "success\n";
    }

    @GetMapping("/stop")
    public String stop() {
        log.info("stop experiment.");
        userService.stop();
        return "success\n";
    }

    @GetMapping("/restart")
    public String restart() {
        log.info("restart experiment.");
        userService.restart();
        return "success\n";
    }
}
