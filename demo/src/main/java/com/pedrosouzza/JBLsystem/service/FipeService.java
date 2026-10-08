package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.client.FipeApiClient;
import com.pedrosouzza.JBLsystem.domain.TipoVeiculoEnum;
import com.pedrosouzza.JBLsystem.dto.MarcaResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FipeService {

    private final FipeApiClient fipeApiClient;
    public FipeService(FipeApiClient fipeApiClient) {
        this.fipeApiClient = fipeApiClient;
    }
    public List<MarcaResponseDTO> listarMarcas(String tipoVeiculo, String tabelaReferencia, String nome) {
        TipoVeiculoEnum tipo = TipoVeiculoEnum.fromString(tipoVeiculo);
        List<MarcaResponseDTO> marcas = fipeApiClient.buscarMarcas(tipo, tabelaReferencia);

        if (nome != null && !nome.isBlank()) {
            return marcas.stream()
                    .filter(m -> m.nome().toLowerCase().contains(nome.toLowerCase()))
                    .toList();
        }

        return marcas;
    }
}
