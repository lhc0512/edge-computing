package cn.edu.scut.config;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataGenerationConfig {

    @Value("${edgeComputing.minExecutionFailureRate}")
    private Double minExecutionFailureRate;

    @Value("${edgeComputing.maxExecutionFailureRate}")
    private Double maxExecutionFailureRate;

    @Value("${edgeComputing.maxCpuCore}")
    private Integer maxCpuCore;

    @Value("${edgeComputing.minCpuCore}")
    private Integer minCpuCore;

    @Value("${edgeComputing.minTaskRate}")
    private Double minTaskRate;

    @Value("${edgeComputing.maxTaskRate}")
    private Double maxTaskRate;

    @Value("${edgeComputing.minTransmissionRate}")
    private Double minTransmissionRate;

    @Value("${edgeComputing.maxTransmissionRate}")
    private Double maxTransmissionRate;

    @Value("${edgeComputing.minTransmissionFailureRate}")
    private Double minTransmissionFailureRate;

    @Value("${edgeComputing.maxTransmissionFailureRate}")
    private Double maxTransmissionFailureRate;

    @Value("${edgeComputing.edgeNodeSeed}")
    private int edgeNodeSeed;

    @Value("${edgeComputing.taskSeed}")
    private int taskSeed;

    @Value("${edgeComputing.minTaskSize}")
    private int minTaskSize;

    @Value("${edgeComputing.maxTaskSize}")
    private int maxTaskSize;

    @Value("${edgeComputing.minTaskComplexity}")
    private int minTaskComplexity;

    @Value("${edgeComputing.maxTaskComplexity}")
    private int maxTaskComplexity;

    @Bean
    public RandomGenerator randomGenerator() {
        var random = new JDKRandomGenerator();
        random.setSeed(edgeNodeSeed);
        return random;
    }

    @Bean
    public UniformRealDistribution executionFailureRandom(RandomGenerator randomGenerator) {
        return new UniformRealDistribution(randomGenerator, minExecutionFailureRate, maxExecutionFailureRate);
    }

    @Bean
    public UniformIntegerDistribution cpuCoreRandom(RandomGenerator randomGenerator) {
        return new UniformIntegerDistribution(randomGenerator, minCpuCore / 4, maxCpuCore / 4);
    }

    @Bean
    public UniformRealDistribution taskRateRandom(RandomGenerator randomGenerator) {
        return new UniformRealDistribution(randomGenerator, minTaskRate, maxTaskRate);
    }

    @Bean
    public UniformRealDistribution transmissionRateRandom(RandomGenerator randomGenerator) {
        return new UniformRealDistribution(randomGenerator, minTransmissionRate, maxTransmissionRate);
    }

    @Bean
    public UniformRealDistribution transmissionFailureRateRandom(RandomGenerator randomGenerator) {
        return new UniformRealDistribution(randomGenerator, minTransmissionFailureRate, maxTransmissionFailureRate);
    }

    @Bean
    public RandomGenerator taskRandomGenerator() {
        var random = new JDKRandomGenerator();
        random.setSeed(taskSeed);
        return random;
    }

    @Bean
    public UniformIntegerDistribution taskSizeRandom(RandomGenerator taskRandomGenerator) {
        return new UniformIntegerDistribution(taskRandomGenerator, minTaskSize, maxTaskSize);
    }

    @Bean
    public UniformIntegerDistribution taskComplexityRandom(RandomGenerator taskRandomGenerator) {
        return new UniformIntegerDistribution(taskRandomGenerator, minTaskComplexity, maxTaskComplexity);
    }
}
