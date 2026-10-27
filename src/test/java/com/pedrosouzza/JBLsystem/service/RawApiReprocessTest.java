package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.client.BrasilApiClient;
import com.pedrosouzza.JBLsystem.client.FipeApiClient;
import com.pedrosouzza.JBLsystem.model.Empresa;
import com.pedrosouzza.JBLsystem.model.RawApiResponse;
import com.pedrosouzza.JBLsystem.model.VeiculoFipe;
import com.pedrosouzza.JBLsystem.repository.EmpresaRepository;
import com.pedrosouzza.JBLsystem.repository.LogIngestaoRepository;
import com.pedrosouzza.JBLsystem.repository.VeiculoFipeRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RawApiReprocessTest {

    @Test
    void reprocessaCnpjDoRawSemChamarBrasilApi() {
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        BrasilApiClient brasilApiClient = mock(BrasilApiClient.class);
        RawApiResponseService rawService = mock(RawApiResponseService.class);
        when(rawService.buscarCnpj(7)).thenReturn(Optional.of(new RawApiResponse(
                7L,
                "12345678000190",
                """
                        {"cnpj":"12345678000190","razao_social":"Acme Ltda","porte":"ME",
                        "cnaes_secundarios":[]}
                        """,
                LocalDateTime.now(),
                200
        )));
        when(empresaRepository.findByCnpj("12345678000190")).thenReturn(Optional.empty());
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        EmpresaService service = new EmpresaService(empresaRepository, brasilApiClient, rawService, new ObjectMapper());
        Empresa resultado = service.reprocessarRespostaBrutaCnpj(7);

        assertNotNull(resultado);
        assertEquals("Acme Ltda", resultado.getRazaoSocial());
        verify(brasilApiClient, never()).buscarCnpj(any());
    }

    @Test
    void reprocessaPrecoFipeDoRawSemChamarFipe() {
        VeiculoFipeRepository veiculoRepository = mock(VeiculoFipeRepository.class);
        FipeApiClient fipeApiClient = mock(FipeApiClient.class);
        FatoPrecoFipeService historicoService = mock(FatoPrecoFipeService.class);
        LogIngestaoRepository logRepository = mock(LogIngestaoRepository.class);
        RawApiResponseService rawService = mock(RawApiResponseService.class);
        VeiculoFipe veiculo = new VeiculoFipe();
        veiculo.setCodigoFipe("003123-4");
        when(rawService.buscarFipe(9)).thenReturn(Optional.of(new RawApiResponse(
                9L,
                "preco:carros:003123-4",
                """
                        [{"codigoFipe":"003123-4","valorFipe":30000.00,
                        "mesReferencia":"janeiro de 2026"}]
                        """,
                LocalDateTime.now(),
                200
        )));
        when(veiculoRepository.findById("003123-4")).thenReturn(Optional.of(veiculo));
        when(veiculoRepository.save(any(VeiculoFipe.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        AtualizacaoPrecoFipeJob job = new AtualizacaoPrecoFipeJob(
                veiculoRepository,
                fipeApiClient,
                historicoService,
                logRepository,
                rawService,
                new ObjectMapper(),
                "carros"
        );
        AtualizacaoPrecoFipeJob.Resultado resultado = job.reprocessarRespostaBruta(9);

        assertEquals("SUCESSO", resultado.status());
        assertEquals(new BigDecimal("30000.00"), veiculo.getValor());
        verify(fipeApiClient, never()).buscarPorCodigo(any(), any());
        verify(historicoService).salvarHistoricoPreco("003123-4", "janeiro de 2026", new BigDecimal("30000.00"));
    }
}
