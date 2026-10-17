package com.vidasalud.bff.controller;

import com.vidasalud.bff.config.SecurityConfig;
import com.vidasalud.bff.security.IdentidadUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica que el BFF limite a cada paciente a sus propias atenciones y propague los errores reales. */
@SuppressWarnings("null")
@WebMvcTest(AppointmentsController.class)
@Import({SecurityConfig.class, IdentidadUsuario.class, ErroresServiciosHandler.class, AppointmentsControllerTest.ClienteFalso.class})
class AppointmentsControllerTest {

    private static final String BASE = "http://appointments";

    @TestConfiguration
    static class ClienteFalso {
        static MockRestServiceServer servidor;

        @Bean
        RestClient appointmentsClient() {
            RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
            servidor = MockRestServiceServer.bindTo(builder).build();
            return builder.build();
        }
    }

    @Autowired
    private MockMvc mvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void preparar() {
        ClienteFalso.servidor.reset();
        when(jwtDecoder.decode("paciente")).thenReturn(Jwt.withTokenValue("paciente").header("alg", "none")
                .subject("sub-ana").claim("oid", "oid-ana").claim("name", "Anita")
                .claim("given_name", "Ana").claim("family_name", "Pérez").build());
        when(jwtDecoder.decode("recepcion")).thenReturn(Jwt.withTokenValue("recepcion").header("alg", "none")
                .subject("sub-rec").claim("oid", "oid-rec").claim("roles", List.of("Recepcionista")).build());
    }

    @Test
    void unPacienteSoloPideSusPropiasAtenciones() throws Exception {
        ClienteFalso.servidor.expect(requestTo(BASE + "/api/appointments?pacienteId=oid-ana"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        mvc.perform(get("/api/appointments").param("pacienteId", "oid-de-otro").header("Authorization", "Bearer paciente"))
                .andExpect(status().isOk());
        ClienteFalso.servidor.verify();
    }

    @Test
    void laRecepcionVeTodasLasAtenciones() throws Exception {
        ClienteFalso.servidor.expect(requestTo(BASE + "/api/appointments"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        mvc.perform(get("/api/appointments").header("Authorization", "Bearer recepcion"))
                .andExpect(status().isOk());
        ClienteFalso.servidor.verify();
    }

    @Test
    void laCitaCreadaPorUnPacienteQuedaASuNombre() throws Exception {
        ClienteFalso.servidor.expect(requestTo(BASE + "/api/appointments"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.pacienteId").value("oid-ana"))
                .andExpect(jsonPath("$.pacienteNombre").value("Ana Pérez"))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        mvc.perform(post("/api/appointments").header("Authorization", "Bearer paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pacienteNombre\":\"Otro Nombre\",\"servicioId\":1,\"pacienteId\":\"oid-de-otro\"}"))
                .andExpect(status().isOk());
        ClienteFalso.servidor.verify();
    }

    @Test
    void propagaElErrorRealDelMicroservicio() throws Exception {
        ClienteFalso.servidor.expect(requestTo(BASE + "/api/appointments/5/status"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        mvc.perform(put("/api/appointments/5/status").header("Authorization", "Bearer recepcion")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CERRADA\"}"))
                .andExpect(status().isBadRequest());
    }
}
