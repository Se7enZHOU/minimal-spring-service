package com.example.minimalspring.service;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.List;

import com.example.minimalspring.dto.NoteRequest;
import com.example.minimalspring.dto.NoteResponse;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class NoteService {

    private static final String BASE_SELECT = """
            SELECT id, title, content, created_at
            FROM note
            """;

    private final JdbcTemplate jdbcTemplate;

    public NoteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NoteResponse> findAll() {
        return jdbcTemplate.query(
                BASE_SELECT + " ORDER BY id",
                (resultSet, rowNum) -> new NoteResponse(
                        resultSet.getLong("id"),
                        resultSet.getString("title"),
                        resultSet.getString("content"),
                        toInstant(resultSet.getTimestamp("created_at"))
                )
        );
    }

    public NoteResponse findById(long id) {
        try {
            return jdbcTemplate.queryForObject(
                    BASE_SELECT + " WHERE id = ?",
                    (resultSet, rowNum) -> new NoteResponse(
                            resultSet.getLong("id"),
                            resultSet.getString("title"),
                            resultSet.getString("content"),
                            toInstant(resultSet.getTimestamp("created_at"))
                    ),
                    id
            );
        } catch (EmptyResultDataAccessException exception) {
            throw new ResponseStatusException(NOT_FOUND, "Note not found", exception);
        }
    }

    public NoteResponse create(NoteRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO note (title, content) VALUES (?, ?)",
                    new String[] {"id"}
            );
            statement.setString(1, request.title());
            statement.setString(2, request.content());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create note");
        }

        return findById(key.longValue());
    }

    public NoteResponse update(long id, NoteRequest request) {
        int updated = jdbcTemplate.update(
                "UPDATE note SET title = ?, content = ? WHERE id = ?",
                request.title(),
                request.content(),
                id
        );

        if (updated == 0) {
            throw new ResponseStatusException(NOT_FOUND, "Note not found");
        }

        return findById(id);
    }

    public void delete(long id) {
        int deleted = jdbcTemplate.update("DELETE FROM note WHERE id = ?", id);

        if (deleted == 0) {
            throw new ResponseStatusException(NOT_FOUND, "Note not found");
        }
    }

    private Instant toInstant(java.sql.Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
