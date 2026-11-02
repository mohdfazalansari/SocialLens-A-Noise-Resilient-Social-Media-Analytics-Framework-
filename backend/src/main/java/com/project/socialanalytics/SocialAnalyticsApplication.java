package com.project.socialanalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SocialAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialAnalyticsApplication.class, args);
    }
}

