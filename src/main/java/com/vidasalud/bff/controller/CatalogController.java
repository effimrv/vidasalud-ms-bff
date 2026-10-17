package com.vidasalud.bff.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private static final @NonNull MediaType JSON = Objects.requireNonNull(MediaType.APPLICATION_JSON);

    private final RestClient client;

    public CatalogController(@NonNull RestClient catalogClient) {
        this.client = Objects.requireNonNull(catalogClient, "catalogClient must not be null");
    }

    @GetMapping("/services")
    public ResponseEntity<String> listServices() {
        return client.get().uri("/api/catalog/services")
                .retrieve().toEntity(String.class);
    }

    @PostMapping("/services")
    public ResponseEntity<String> createService(@RequestBody @NonNull String body) {
        return client.post().uri("/api/catalog/services")
                .contentType(JSON)
                .body(Objects.requireNonNull(body, "body must not be null")).retrieve().toEntity(String.class);
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<String> updateService(@PathVariable("id") @NonNull Long id, @RequestBody @NonNull String body) {
        return client.put().uri("/api/catalog/services/{id}", id)
                .contentType(JSON)
                .body(Objects.requireNonNull(body, "body must not be null")).retrieve().toEntity(String.class);
    }
}
