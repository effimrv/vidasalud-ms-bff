package com.vidasalud.bff.controller;

import com.vidasalud.bff.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica el rol por defecto de las cuentas recién registradas y la respuesta de /api/me con claims incompletos. */
@WebMvcTest(MeController.class)
@Import(SecurityConfig.class)
class MeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void unaCuentaSinRolesEsPaciente() throws Exception {
        token("nuevo", Jwt.withTokenValue("nuevo").header("alg", "none")
                .subject("abc").claim("email", "ana@gmail.com").build());

        mvc.perform(get("/api/me").header("Authorization", "Bearer nuevo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("Paciente"))
                .andExpect(jsonPath("$.usuario").value("ana@gmail.com"))
                .andExpect(jsonPath("$.nombre").value("ana@gmail.com"));
    }

    @Test
    void respetaLosRolesAsignadosEnEntra() throws Exception {
        token("admin", Jwt.withTokenValue("admin").header("alg", "none")
                .subject("xyz").claim("name", "Ara").claim("preferred_username", "ara@duocuc.cl")
                .claim("roles", List.of("Admin")).build());

        mvc.perform(get("/api/me").header("Authorization", "Bearer admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ara"))
                .andExpect(jsonPath("$.roles[0]").value("Admin"))
                .andExpect(jsonPath("$.roles.length()").value(1));
    }

    @Test
    void unPacienteNoPuedeEditarLaInformacionDelCentro() throws Exception {
        token("nuevo", Jwt.withTokenValue("nuevo").header("alg", "none").subject("abc").build());

        mvc.perform(put("/api/info").header("Authorization", "Bearer nuevo")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    private void token(String valor, Jwt jwt) {
        when(jwtDecoder.decode(valor)).thenReturn(jwt);
    }
}
