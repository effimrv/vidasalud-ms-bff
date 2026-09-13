package com.vidasalud.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient appointmentsClient(@Value("${services.appointments-url}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }

    @Bean
    public RestClient catalogClient(@Value("${services.catalog-url}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }
}
