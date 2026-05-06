package com.example.minimalspring.dto;

import java.time.Instant;

public record DatabasePingResponse(
        String status,
        String databaseProduct,
        String databaseUrl,
        int validationResult,
        Instant timestamp
) {
}
