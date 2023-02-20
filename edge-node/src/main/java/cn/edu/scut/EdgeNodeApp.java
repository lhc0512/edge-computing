package cn.edu.scut;

import cn.edu.scut.utils.SpringBeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
public class EdgeNodeApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(EdgeNodeApp.class, args);
        SpringBeanUtils.setApplicationContext(context);
    }
}