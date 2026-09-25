package com.pedrosouzza.JBLsystem.client;

import com.pedrosouzza.JBLsystem.dto.BrasilApiCnpjDto;
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

    public BrasilApiCnpjDto buscarCnpj(String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("\\D", "");

                return restClient.get()
                        .uri("/cnpj}", cnpjLimpo)
                        .retrieve()
                        .body(BrasilApiCnpjDto.class);
    }
}
