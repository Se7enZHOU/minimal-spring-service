package com.example.minimalspring.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;

import javax.sql.DataSource;

import com.example.minimalspring.dto.DatabasePingResponse;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseService(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public DatabasePingResponse ping() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            Integer validationResult = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            return new DatabasePingResponse(
                    "UP",
                    metaData.getDatabaseProductName(),
                    metaData.getURL(),
                    validationResult == null ? 0 : validationResult,
                    Instant.now()
            );
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to connect to the database", exception);
        }
    }
}
