package com.vidasalud.bff.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final RestClient client;

    public CatalogController(RestClient catalogClient) {
        this.client = catalogClient;
    }

    @GetMapping("/services")
    public ResponseEntity<String> listServices() {
        return client.get().uri("/api/catalog/services")
                .retrieve().toEntity(String.class);
    }

    @PostMapping("/services")
    public ResponseEntity<String> createService(@RequestBody String body) {
        return client.post().uri("/api/catalog/services")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().toEntity(String.class);
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<String> updateService(@PathVariable Long id, @RequestBody String body) {
        return client.put().uri("/api/catalog/services/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().toEntity(String.class);
    }
}
