package com.example.minimalspring.controller;

import com.example.minimalspring.dto.DatabasePingResponse;
import com.example.minimalspring.service.DatabaseService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/db")
public class DatabaseController {

    private final DatabaseService databaseService;

    public DatabaseController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GetMapping("/ping")
    public DatabasePingResponse ping() {
        return databaseService.ping();
    }
}
