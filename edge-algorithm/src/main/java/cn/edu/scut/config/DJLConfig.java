package cn.edu.scut.config;

import ai.djl.engine.Engine;
import ai.djl.ndarray.NDManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class DJLConfig {

    @Value("${edgeComputing.seed}")
    Integer seed;

    @Value("${spring.application.name}")
    String name;

    @Bean
    public Engine engine() {
        Engine engine = Engine.getInstance();
        engine.setRandomSeed(seed);
        return engine;
    }

    @Bean
    public NDManager manager(Engine engine) {
        return engine.newBaseManager();
    }

    @Bean
    public Random bufferRandom(){
        var random = new Random();
        random.setSeed(seed);
        return random;
    }
}
