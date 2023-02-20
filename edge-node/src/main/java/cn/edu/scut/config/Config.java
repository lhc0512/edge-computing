package cn.edu.scut.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Configuration
@Slf4j
public class Config {

    @Value("${edgeComputing.reliabilitySeed}")
    Integer reliabilitySeed;

    @Value("${edgeComputing.schedulerSeed}")
    Integer schedulerSeed;

    @Value("${spring.application.name}")
    String name;

    @Bean
    public Random reliabilityRandom() {
        Integer id = Integer.parseInt(name.split("-")[2]);
        return new Random(reliabilitySeed + id);
    }

    @Bean
    public Random schedulerRandom() {
        Integer id = Integer.parseInt(name.split("-")[2]);
        return new Random(schedulerSeed + id);
    }

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}