package com.example.minimalspring.controller;

import java.net.URI;
import java.util.List;

import com.example.minimalspring.dto.NoteRequest;
import com.example.minimalspring.dto.NoteResponse;
import com.example.minimalspring.service.NoteService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<NoteResponse> list() {
        return noteService.findAll();
    }

    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable long id) {
        return noteService.findById(id);
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(@Valid @RequestBody NoteRequest request) {
        NoteResponse created = noteService.create(request);
        return ResponseEntity
                .created(URI.create("/api/notes/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable long id, @Valid @RequestBody NoteRequest request) {
        return noteService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
