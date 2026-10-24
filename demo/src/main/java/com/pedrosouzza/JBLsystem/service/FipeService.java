package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.client.FipeApiClient;
import com.pedrosouzza.JBLsystem.domain.FatoPrecoFipe;
import com.pedrosouzza.JBLsystem.domain.TipoVeiculoEnum;
import com.pedrosouzza.JBLsystem.dto.FipeValorDTO;
import com.pedrosouzza.JBLsystem.dto.MarcaResponseDTO;
import com.pedrosouzza.JBLsystem.exception.ResourceNotFoundException;
import com.pedrosouzza.JBLsystem.repository.FatoPrecoFipeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FipeService {

    private final FipeApiClient fipeApiClient;
    private final FatoPrecoFipeRepository fatoPrecoFipeRepository;

    public FipeService(FipeApiClient fipeApiClient, FatoPrecoFipeRepository fatoPrecoFipeRepository) {
        this.fipeApiClient = fipeApiClient;
        this.fatoPrecoFipeRepository = fatoPrecoFipeRepository;
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

    public List<FipeValorDTO> obterPrecoPorCodigo(String tipoVeiculo, String codigoFipe) {
        List<FipeValorDTO> valores = fipeApiClient.buscarPorCodigo(tipoVeiculo, codigoFipe);

        if (valores == null || valores.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum veículo encontrado para o código FIPE: " + codigoFipe);
        }

        return valores;
    }

    public void salvarHistoricoPreco(String codigoFipe, String mesReferencia, BigDecimal valor) {
        boolean jaExiste = fatoPrecoFipeRepository.findByCodigoFipeAndMesReferencia(codigoFipe, mesReferencia).isPresent();

        if (!jaExiste) {
            FatoPrecoFipe historico = new FatoPrecoFipe(
                    null,
                    codigoFipe,
                    mesReferencia,
                    valor,
                    LocalDateTime.now()
            );

            fatoPrecoFipeRepository.save(historico);
        }
    }
}
