package com.longcovidspa.backend.controler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncScheduleConfig {
    @Bean("privacyExecutor")
    public Executor privacyExecutor() {
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(3);
        ex.setMaxPoolSize(6);
        ex.setQueueCapacity(50);
        ex.setThreadNamePrefix("privacy-");
        ex.initialize();
        return ex;
    }
}
