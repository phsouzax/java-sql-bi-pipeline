package com.pedrosouzza.JBLsystem.client;

import tools.jackson.databind.ObjectMapper;
import com.pedrosouzza.JBLsystem.dto.FipePrecoDTO;
import com.pedrosouzza.JBLsystem.service.RawApiResponseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FipeApiClientTest {

    private static final String PRECO_JSON = """
            {
              "brand": "Ford",
              "codeFipe": "003123-4",
              "fuel": "Gasolina",
              "fuelAcronym": "G",
              "model": "Fiesta",
              "modelYear": 2014,
              "price": "R$ 30.000,00",
              "referenceMonth": "janeiro de 2026",
              "vehicleType": 1
            }
            """;

    private RawApiResponseService rawApiResponseService;
    private MockRestServiceServer mockServer;
    private FipeApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        rawApiResponseService = mock(RawApiResponseService.class);
        client = new FipeApiClient(
                builder,
                new ObjectMapper(),
                rawApiResponseService,
                3,
                Duration.ZERO,
                Duration.ofHours(6)
        );
    }

    @Test
    void deveRepetirFalha5xxAteRecuperarEUsarCacheDaMesmaChave() {
        String url = "https://fipe.parallelum.com.br/api/v2/cars/003123-4/years/2014-1?reference=308";
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"indisponível\"}"));
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(PRECO_JSON, MediaType.APPLICATION_JSON));

        FipePrecoDTO primeiraResposta = client.buscarPreco("carros", "003123-4", "2014-1", "308");
        FipePrecoDTO respostaDoCache = client.buscarPreco("carros", "003123-4", "2014-1", "308");

        assertEquals("R$ 30.000,00", primeiraResposta.price());
        assertEquals(primeiraResposta, respostaDoCache);
        mockServer.verify();
        verify(rawApiResponseService, times(2)).registrarFipe(
                org.mockito.ArgumentMatchers.startsWith("preco:"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyInt()
        );
    }

    @Test
    void naoDeveRepetirResposta4xx() {
        String url = "https://fipe.parallelum.com.br/api/v2/cars/003123-4/years/2014-1?reference=308";
        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"não encontrado\"}"));

        assertThrows(org.springframework.web.client.RestClientResponseException.class,
                () -> client.buscarPreco("carros", "003123-4", "2014-1", "308"));

        mockServer.verify();
        verify(rawApiResponseService, times(1)).registrarFipe(
                org.mockito.ArgumentMatchers.startsWith("preco:"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyInt()
        );
    }
}
