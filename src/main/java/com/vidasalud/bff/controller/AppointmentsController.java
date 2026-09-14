package com.vidasalud.bff.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {

    private final RestClient client;

    public AppointmentsController(@NonNull RestClient appointmentsClient) {
        this.client = Objects.requireNonNull(appointmentsClient, "appointmentsClient must not be null");
    }

    @GetMapping
    public ResponseEntity<String> list(@RequestParam Map<String, String> filtros) {
        return client.get().uri(b -> {
            b.path("/api/appointments");
            filtros.forEach(b::queryParam);
            return b.build();
        }).retrieve().toEntity(String.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable("id") @NonNull Long id) {
        return client.get().uri("/api/appointments/{id}", id)
                .retrieve().toEntity(String.class);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody @NonNull String body) {
        MediaType jsonType = MediaType.parseMediaType("application/json");
        return client.post().uri("/api/appointments")
                .contentType(jsonType)
                .body(Objects.requireNonNull(body, "body must not be null")).retrieve().toEntity(String.class);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> changeStatus(@PathVariable("id") @NonNull Long id, @RequestBody @NonNull String body) {
        MediaType jsonType = MediaType.parseMediaType("application/json");
        return client.put().uri("/api/appointments/{id}/status", id)
                .contentType(jsonType)
                .body(Objects.requireNonNull(body, "body must not be null")).retrieve().toEntity(String.class);
    }
}
