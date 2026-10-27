package com.pedrosouzza.JBLsystem.client;

import java.util.Map;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.pedrosouzza.JBLsystem.service.RawApiResponseService;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

@Component
public class BrasilApiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final RawApiResponseService rawApiResponseService;

    public BrasilApiClient(ObjectMapper objectMapper,
                           RawApiResponseService rawApiResponseService) {
        this.restClient = RestClient.builder()
                .baseUrl("https://brasilapi.com.br/api/cnpj/v1")
                .build();
        this.objectMapper = objectMapper;
        this.rawApiResponseService = rawApiResponseService;
    }

    public Map<String, Object> buscarCnpj(String cnpj) {
        return restClient.get()
                .uri("/{cnpj}", cnpj)
                .exchange((request, response) -> {
                    byte[] body = response.getBody().readAllBytes();
                    rawApiResponseService.registrarCnpj(cnpj, new String(body, StandardCharsets.UTF_8),
                            response.getStatusCode().value());
                    ensureSuccessful(response.getStatusCode(), response.getHeaders(), body);
                    return objectMapper.readValue(body, new TypeReference<>() {});
                });
    }

    private void ensureSuccessful(HttpStatusCode status, org.springframework.http.HttpHeaders headers, byte[] body) {
        if (!status.is2xxSuccessful()) {
            throw new RestClientResponseException(
                    "BrasilAPI respondeu com HTTP " + status.value(),
                    status.value(),
                    status.toString(),
                    headers,
                    body,
                    StandardCharsets.UTF_8
            );
        }
    }
}
