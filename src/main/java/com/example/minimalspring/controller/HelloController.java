package com.example.minimalspring.controller;

import com.example.minimalspring.dto.HelloResponse;
import com.example.minimalspring.service.GreetingService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final GreetingService greetingService;

    public HelloController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/hello")
    public HelloResponse hello() {
        return greetingService.buildHelloResponse();
    }
}
