package com.example.demojsonlogging.configuration;

import com.example.demojsonlogging.logger.AsynTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncTaskConfiguration {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();
        threadPoolExecutor.setThreadNamePrefix("Task-");
        threadPoolExecutor.setCorePoolSize(3);
        threadPoolExecutor.setTaskDecorator(new AsynTaskDecorator());
        return threadPoolExecutor;
    }
}
