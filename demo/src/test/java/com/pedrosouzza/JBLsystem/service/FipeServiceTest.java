package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.client.FipeApiClient;
import com.pedrosouzza.JBLsystem.dto.FipeValorDTO;
import com.pedrosouzza.JBLsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FipeServiceTest {

    @Mock
    private FipeApiClient fipeApiClient;

    @InjectMocks
    private FipeService fipeService;

    @Test
    @DisplayName("Deve retornar o valor FIPE mapeado com sucesso")
    void deveRetornarValorFipeComSucesso() {
        // Arrange
        String tipoVeiculo = "carros";
        String codigoFipe = "004001-1";

        FipeValorDTO mockDto = FipeValorDTO.builder()
                .codigoFipe(codigoFipe)
                .valorFipe(new BigDecimal("58094.00"))
                .mesReferencia(LocalDate.of(2026, 10, 1))
                .build();

        Mockito.when(fipeApiClient.buscarPorCodigo(tipoVeiculo, codigoFipe))
                .thenReturn(List.of(mockDto));

        // Act
        List<FipeValorDTO> resultado = fipeService.obterPrecoPorCodigo(tipoVeiculo, codigoFipe);

        // Assert
        Assertions.assertNotNull(resultado);
        Assertions.assertFalse(resultado.isEmpty());
        Assertions.assertEquals(new BigDecimal("58094.00"), resultado.get(0).getValorFipe());
        Assertions.assertEquals(LocalDate.of(2026, 10, 1), resultado.get(0).getMesReferencia());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando a API retornar lista vazia")
    void deveLancarExcecaoQuandoListaVazia() {
        // Arrange
        String tipoVeiculo = "carros";
        String codigoInvalido = "999999-9";

        Mockito.when(fipeApiClient.buscarPorCodigo(tipoVeiculo, codigoInvalido))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            fipeService.obterPrecoPorCodigo(tipoVeiculo, codigoInvalido);
        });
    }
}