package com.vidasalud.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Objects;

@Configuration
public class RestClientConfig {

    /** Sin timeouts, un microservicio colgado dejaría al BFF esperando para siempre. */
    private static final @NonNull Duration TIEMPO_CONEXION = Objects.requireNonNull(Duration.ofSeconds(5));
    private static final @NonNull Duration TIEMPO_RESPUESTA = Objects.requireNonNull(Duration.ofSeconds(15));

    @Bean
    public RestClient appointmentsClient(@Value("${services.appointments-url}") @NonNull String url) {
        return cliente(Objects.requireNonNull(url, "services.appointments-url must not be null"));
    }

    @Bean
    public RestClient catalogClient(@Value("${services.catalog-url}") @NonNull String url) {
        return cliente(Objects.requireNonNull(url, "services.catalog-url must not be null"));
    }

    private static RestClient cliente(@NonNull String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(TIEMPO_CONEXION);
        fabrica.setReadTimeout(TIEMPO_RESPUESTA);
        return RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }
}
