package com.example.minimalspring.dto;

import java.time.Instant;

public record NoteResponse(
        Long id,
        String title,
        String content,
        Instant createdAt
) {
}
