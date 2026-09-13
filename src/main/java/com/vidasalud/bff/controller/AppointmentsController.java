package com.vidasalud.bff.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {

    private final RestClient client;

    public AppointmentsController(RestClient appointmentsClient) {
        this.client = appointmentsClient;
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
    public ResponseEntity<String> getById(@PathVariable Long id) {
        return client.get().uri("/api/appointments/{id}", id)
                .retrieve().toEntity(String.class);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody String body) {
        return client.post().uri("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().toEntity(String.class);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> changeStatus(@PathVariable Long id, @RequestBody String body) {
        return client.put().uri("/api/appointments/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().toEntity(String.class);
    }
}
