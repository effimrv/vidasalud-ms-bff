package com.vidasalud.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient appointmentsClient(@Value("${services.appointments-url}") @NonNull String url) {
        return RestClient.builder().baseUrl(Objects.requireNonNull(url, "services.appointments-url must not be null")).build();
    }

    @Bean
    public RestClient catalogClient(@Value("${services.catalog-url}") @NonNull String url) {
        return RestClient.builder().baseUrl(Objects.requireNonNull(url, "services.catalog-url must not be null")).build();
    }
}
