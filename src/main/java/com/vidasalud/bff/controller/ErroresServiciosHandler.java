package com.vidasalud.bff.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Objects;

/**
 * Propaga al frontend el código real de los microservicios (400 transición inválida, 404 no encontrada...)
 * en vez de convertir todo en 500, y responde 503 si el microservicio no está disponible.
 */
@RestControllerAdvice
public class ErroresServiciosHandler {

    private static final Logger log = LoggerFactory.getLogger(ErroresServiciosHandler.class);

    private static final @NonNull MediaType JSON = Objects.requireNonNull(MediaType.APPLICATION_JSON);

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<String> errorDelServicio(RestClientResponseException e) {
        return ResponseEntity.status(e.getStatusCode())
                .contentType(JSON)
                .body(e.getResponseBodyAsString());
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<String> servicioNoDisponible(ResourceAccessException e) {
        log.error("Microservicio no disponible: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(JSON)
                .body("{\"mensaje\":\"El servicio no está disponible en este momento\"}");
    }
}
