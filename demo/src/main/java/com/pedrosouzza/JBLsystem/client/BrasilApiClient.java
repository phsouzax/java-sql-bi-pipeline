package com.pedrosouzza.JBLsystem.client;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BrasilApiClient {

    private final RestClient restClient;

    public BrasilApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://brasilapi.com.br/api/cnpj/v1")
                .build();
    }

    public Map<String, Object> buscarCnpj(String cnpj) {
        return restClient.get()
                .uri("/{cnpj}", cnpj)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}