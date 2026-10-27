package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.client.FipeApiClient;
import com.pedrosouzza.JBLsystem.dto.FipePrecoDTO;
import com.pedrosouzza.JBLsystem.dto.FipeValorDTO;
import com.pedrosouzza.JBLsystem.exception.ResourceNotFoundException;
import com.pedrosouzza.JBLsystem.model.ConsultaPrecoFipe;
import com.pedrosouzza.JBLsystem.model.RawApiResponse;
import com.pedrosouzza.JBLsystem.repository.ConsultaPrecoFipeRepository;
import com.pedrosouzza.JBLsystem.repository.FatoPrecoFipeRepository;
import com.pedrosouzza.JBLsystem.service.RawApiResponseService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class FipeServiceTest {

    @Mock
    private FipeApiClient fipeApiClient;

    @Mock
    private FatoPrecoFipeRepository fatoPrecoFipeRepository;

    @Mock
    private ConsultaPrecoFipeRepository consultaPrecoFipeRepository;

    @Mock
    private RawApiResponseService rawApiResponseService;

    private FipeService fipeService;

    @BeforeEach
    void setUp() {
        fipeService = new FipeService(fipeApiClient, fatoPrecoFipeRepository,
                consultaPrecoFipeRepository, rawApiResponseService, new ObjectMapper(), Duration.ofHours(6));
    }

    @Test
    @DisplayName("Deve retornar o valor FIPE mapeado com sucesso")
    void deveRetornarValorFipeComSucesso() {
        // Arrange
        String tipoVeiculo = "carros";
        String codigoFipe = "004001-1";

        FipeValorDTO mockDto = FipeValorDTO.builder()
                .codigoFipe(codigoFipe)
                .valorFipe(new BigDecimal("58094.00"))
                .mesReferencia("10/2026")
                .build();

        Mockito.when(fipeApiClient.buscarPorCodigo(tipoVeiculo, codigoFipe))
                .thenReturn(List.of(mockDto));

        // Act
        List<FipeValorDTO> resultado = fipeService.obterPrecoPorCodigo(tipoVeiculo, codigoFipe);

        // Assert
        Assertions.assertNotNull(resultado);
        Assertions.assertFalse(resultado.isEmpty());
        Assertions.assertEquals(new BigDecimal("58094.00"), resultado.get(0).getValorFipe());
        Assertions.assertEquals("10/2026", resultado.get(0).getMesReferencia());
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

    @Test
    @DisplayName("Deve gravar consulta pontual fora do histórico padrão")
    void devePersistirConsultaPontualEmTabelaSeparada() {
        FipePrecoDTO preco = new FipePrecoDTO("Fiat", "001313-7", "Flex", "F", "Punto Attractive",
                2012, "R$ 32.474,00", "outubro/2026", 1);
        Mockito.when(consultaPrecoFipeRepository.findByTipoVeiculoAndCodigoFipeAndCodigoAnoAndCodigoReferencia(
                "cars", "001313-7", "2012-5", "338")).thenReturn(Optional.empty());
        Mockito.when(fipeApiClient.buscarPreco("cars", "001313-7", "2012-5", "338")).thenReturn(preco);

        FipePrecoDTO resultado = fipeService.buscarPreco("carros", "001313-7", "2012-5", "338");

        Assertions.assertEquals(preco, resultado);
        Mockito.verify(consultaPrecoFipeRepository).save(Mockito.any(ConsultaPrecoFipe.class));
        Mockito.verifyNoInteractions(fatoPrecoFipeRepository);
    }

    @Test
    @DisplayName("Deve responder da tabela de consulta enquanto o cache persistente estiver válido")
    void deveReutilizarConsultaPontualPersistida() {
        FipePrecoDTO preco = new FipePrecoDTO("Fiat", "001313-7", "Flex", "F", "Punto Attractive",
                2012, "R$ 32.474,00", "outubro/2026", 1);
        ConsultaPrecoFipe consulta = new ConsultaPrecoFipe(
                "cars", "001313-7", "2012-5", "338", preco, LocalDateTime.now());
        Mockito.when(consultaPrecoFipeRepository.findByTipoVeiculoAndCodigoFipeAndCodigoAnoAndCodigoReferencia(
                "cars", "001313-7", "2012-5", "338")).thenReturn(Optional.of(consulta));

        FipePrecoDTO resultado = fipeService.buscarPreco("cars", "001313-7", "2012-5", "338");

        Assertions.assertEquals("001313-7", resultado.codeFipe());
        Assertions.assertEquals("outubro/2026", resultado.referenceMonth());
        Assertions.assertTrue(resultado.price().contains("32.474,00"));
        Mockito.verifyNoInteractions(fipeApiClient);
        Mockito.verifyNoInteractions(fatoPrecoFipeRepository);
    }

    @Test
    @DisplayName("Deve reprocessar raw de consulta pontual na tabela separada sem chamar a API")
    void deveReprocessarConsultaPontualBrutaSemChamadaExterna() {
        String json = """
                {"brand":"Fiat","codeFipe":"001313-7","fuel":"Flex","fuelAcronym":"F",
                 "model":"Punto ATTRACTIVE 1.4","modelYear":2012,"price":"R$ 32.474,00",
                 "referenceMonth":"outubro/2026","vehicleType":1}
                """;
        Mockito.when(rawApiResponseService.buscarFipe(42)).thenReturn(Optional.of(new RawApiResponse(
                42L, "preco:cars:001313-7:2012-5:338", json, LocalDateTime.now(), 200)));
        Mockito.when(consultaPrecoFipeRepository.findByTipoVeiculoAndCodigoFipeAndCodigoAnoAndCodigoReferencia(
                "cars", "001313-7", "2012-5", "338")).thenReturn(Optional.empty());

        FipePrecoDTO resultado = fipeService.reprocessarConsultaBruta(42);

        Assertions.assertEquals("001313-7", resultado.codeFipe());
        Mockito.verify(consultaPrecoFipeRepository).save(Mockito.any(ConsultaPrecoFipe.class));
        Mockito.verifyNoInteractions(fipeApiClient);
        Mockito.verifyNoInteractions(fatoPrecoFipeRepository);
    }
}
