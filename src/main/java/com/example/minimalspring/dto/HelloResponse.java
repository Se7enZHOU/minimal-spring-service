package com.example.minimalspring.dto;

import java.time.Instant;
import java.util.List;

public record HelloResponse(
        String application,
        String message,
        List<String> activeProfiles,
        Instant timestamp
) {
}
