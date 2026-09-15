package com.tijetravel.tijefront.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ConfiguracionApi {
    @Bean
    public RestClient clienteBackend(@Value("${app.backend.url}") String urlBackend) {
        SimpleClientHttpRequestFactory solicitudes = new SimpleClientHttpRequestFactory();
        solicitudes.setConnectTimeout(5000);
        solicitudes.setReadTimeout(5000);
        return RestClient.builder()
                .baseUrl(urlBackend)
                .requestFactory(solicitudes)
                .build();
    }
}
