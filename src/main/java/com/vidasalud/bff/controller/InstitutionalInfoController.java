package com.vidasalud.bff.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@RestController
@RequestMapping("/api/info")
public class InstitutionalInfoController {

    private final RestClient client;

    public InstitutionalInfoController(@NonNull RestClient catalogClient) {
        this.client = Objects.requireNonNull(catalogClient, "catalogClient must not be null");
    }

    @GetMapping
    public ResponseEntity<String> obtener() {
        return client.get().uri("/api/info")
                .retrieve().toEntity(String.class);
    }

    @PutMapping
    public ResponseEntity<String> actualizar(@RequestBody @NonNull String body) {
        MediaType jsonType = MediaType.parseMediaType("application/json");
        return client.put().uri("/api/info")
                .contentType(jsonType)
                .body(Objects.requireNonNull(body, "body must not be null")).retrieve().toEntity(String.class);
    }
}
