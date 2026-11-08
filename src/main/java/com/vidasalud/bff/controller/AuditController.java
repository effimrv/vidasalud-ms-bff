package com.vidasalud.bff.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Objects;

/**
 * Proxy de solo lectura hacia ms-audit. La autorización (Admin y Auditor) la aplica SecurityConfig.
 * ms-audit valida el JWT por su cuenta, así que el BFF reenvía el mismo token.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final RestClient client;

    public AuditController(@NonNull RestClient auditClient) {
        this.client = Objects.requireNonNull(auditClient, "auditClient must not be null");
    }

    @GetMapping("/eventos")
    public ResponseEntity<String> eventos(@RequestParam(defaultValue = "50") int limite,
                                          @RequestHeader(HttpHeaders.AUTHORIZATION) String autorizacion) {
        return client.get().uri(b -> b.path("/api/audit/eventos").queryParam("limite", limite).build())
                .header(HttpHeaders.AUTHORIZATION, autorizacion)
                .retrieve().toEntity(String.class);
    }

    @GetMapping("/citas/{citaId}/timeline")
    public ResponseEntity<String> timeline(@PathVariable("citaId") @NonNull Long citaId,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String autorizacion) {
        return client.get().uri("/api/audit/citas/{id}/timeline", citaId)
                .header(HttpHeaders.AUTHORIZATION, autorizacion)
                .retrieve().toEntity(String.class);
    }
}
