package com.vonage.smsjourneyg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor(@Value("${async.journey.core-pool-size}") int corePoolSize, @Value("${async.journey.max-pool-size}") int maxPoolSize, @Value("${async.journey.queue-capacity}") int queueCapacity) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setRejectedExecutionHandler( new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}
