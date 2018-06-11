package com.api;

import com.api.statistics.service.PeriodStatistics;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean
    public ConcurrentHashMap<LocalDateTime, PeriodStatistics> recentStatistics() {
        return new ConcurrentHashMap<>();
    }
}
