package com.example.minimalspring.service;

import java.time.Instant;
import java.util.Arrays;

import com.example.minimalspring.config.AppProperties;
import com.example.minimalspring.dto.HelloResponse;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    private final Environment environment;
    private final AppProperties appProperties;

    public GreetingService(Environment environment, AppProperties appProperties) {
        this.environment = environment;
        this.appProperties = appProperties;
    }

    public HelloResponse buildHelloResponse() {
        return new HelloResponse(
                environment.getProperty("spring.application.name", "minimal-spring-service"),
                appProperties.getMessage(),
                Arrays.stream(environment.getActiveProfiles()).toList(),
                Instant.now()
        );
    }
}
