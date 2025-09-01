package com.longcovidspa.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpClientConfig {
    @Bean
    WebClient aiWebClient(@Value("${ai.scoring.url}") String base) {
        return WebClient.builder().baseUrl(base).build();
    }
}
