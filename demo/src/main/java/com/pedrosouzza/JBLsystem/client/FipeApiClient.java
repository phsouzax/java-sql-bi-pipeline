package com.pedrosouzza.JBLsystem.client;

import com.pedrosouzza.JBLsystem.dto.MarcaResponseDTO;
import com.pedrosouzza.JBLsystem.domain.TipoVeiculoEnum;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class FipeApiClient {

    private final RestClient restClient;

    public FipeApiClient(RestClient.Builder restClientBuider) {
        this.restClient = restClientBuider
                .baseUrl("https://parallelum.com.br/fipe/api/v1")
                .build();
    }
    public List<MarcaResponseDTO> buscarMarcas(TipoVeiculoEnum tipo, String tabelaReferencia) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/{tipo}/marcas");
                    if (tabelaReferencia != null && !tabelaReferencia.isBlank()) {
                        uriBuilder.queryParam("tabela_referencia", tabelaReferencia);
                    }
                    return uriBuilder.build(tipo.getCodigoFipe());
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<MarcaResponseDTO>>() {});
    }
}
