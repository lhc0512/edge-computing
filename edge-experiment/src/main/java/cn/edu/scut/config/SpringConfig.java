package cn.edu.scut.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Configuration
public class SpringConfig {
    @Value("${edgeComputing.seed}")
    Integer seed;

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Random schedulerRandom() {
        return new Random(seed);
    }
}