package com.vidasalud.bff.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vidasalud.bff.security.IdentidadUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Proxy hacia ms-appointments. Un paciente solo ve, crea y cancela sus propias atenciones: el BFF fija el
 * pacienteId, su nombre y su correo a partir del token, sin importar lo que envíe el frontend.
 * Admin y Recepcionista ven todas; cada cambio queda registrado con quién lo hizo.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {

    private static final String PARAM_PACIENTE = "pacienteId";
    private static final String ESTADO_CANCELADA = "CANCELADA";
    private static final @NonNull MediaType JSON = Objects.requireNonNull(MediaType.APPLICATION_JSON);

    private final RestClient client;
    private final IdentidadUsuario identidad;
    private final ObjectMapper objectMapper;

    public AppointmentsController(@NonNull RestClient appointmentsClient, IdentidadUsuario identidad, ObjectMapper objectMapper) {
        this.client = Objects.requireNonNull(appointmentsClient, "appointmentsClient must not be null");
        this.identidad = identidad;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<String> list(@RequestParam Map<String, String> filtros, Authentication autenticacion) {
        Map<String, String> parametros = new HashMap<>(filtros);
        if (!identidad.esPersonal(autenticacion)) {
            parametros.put(PARAM_PACIENTE, identidad.idPaciente(autenticacion));
        }
        return client.get().uri(b -> {
            b.path("/api/appointments");
            parametros.forEach(b::queryParam);
            return b.build();
        }).retrieve().toEntity(String.class);
    }

    /** Horas de un día para una prestación (cualquier usuario autenticado). */
    @GetMapping("/disponibilidad")
    public ResponseEntity<String> disponibilidad(@RequestParam Long servicioId, @RequestParam String fecha) {
        return client.get().uri(b -> b.path("/api/appointments/disponibilidad")
                        .queryParam("servicioId", servicioId).queryParam("fecha", fecha).build())
                .retrieve().toEntity(String.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable("id") @NonNull Long id, Authentication autenticacion) {
        return client.get().uri(b -> {
            b.path("/api/appointments/{id}");
            if (!identidad.esPersonal(autenticacion)) {
                b.queryParam(PARAM_PACIENTE, identidad.idPaciente(autenticacion));
            }
            return b.build(id);
        }).retrieve().toEntity(String.class);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody @NonNull String body, Authentication autenticacion) throws IOException {
        String cuerpo = Objects.requireNonNull(body, "body must not be null");
        if (!identidad.esPersonal(autenticacion)) {
            ObjectNode cita = (ObjectNode) objectMapper.readTree(cuerpo);
            cita.put(PARAM_PACIENTE, identidad.idPaciente(autenticacion));
            cita.put("pacienteNombre", identidad.nombreCompleto(autenticacion));
            cita.put("pacienteCorreo", identidad.correo(autenticacion));
            cuerpo = objectMapper.writeValueAsString(cita);
        }
        return client.post().uri("/api/appointments")
                .contentType(JSON)
                .body(Objects.requireNonNull(cuerpo))
                .retrieve().toEntity(String.class);
    }

    /** El personal puede cancelar cualquier cita; un paciente, solo cancelar las suyas. */
    @PutMapping("/{id}/status")
    public ResponseEntity<String> changeStatus(@PathVariable("id") @NonNull Long id, @RequestBody @NonNull String body,
                                               Authentication autenticacion) throws IOException {
        ObjectNode cambio = (ObjectNode) objectMapper.readTree(Objects.requireNonNull(body, "body must not be null"));
        if (!identidad.esPersonal(autenticacion) && !ESTADO_CANCELADA.equals(cambio.path("status").asText())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Un paciente solo puede cancelar sus citas");
        }
        cambio.put("responsable", identidad.responsable(autenticacion));
        String cuerpo = objectMapper.writeValueAsString(cambio);
        return client.put().uri(b -> {
                    b.path("/api/appointments/{id}/status");
                    if (!identidad.esPersonal(autenticacion)) {
                        b.queryParam(PARAM_PACIENTE, identidad.idPaciente(autenticacion));
                    }
                    return b.build(id);
                })
                .contentType(JSON)
                .body(Objects.requireNonNull(cuerpo))
                .retrieve().toEntity(String.class);
    }
}
