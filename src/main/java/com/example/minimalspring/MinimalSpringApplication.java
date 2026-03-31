package com.example.minimalspring;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MinimalSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinimalSpringApplication.class, args);
    }
}
