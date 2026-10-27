package com.pedrosouzza.JBLsystem.client;

import com.pedrosouzza.JBLsystem.dto.FipeValorDTO;
import com.pedrosouzza.JBLsystem.dto.FipePrecoDTO;
import com.pedrosouzza.JBLsystem.dto.MarcaResponseDTO;
import com.pedrosouzza.JBLsystem.domain.TipoVeiculoEnum;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.pedrosouzza.JBLsystem.service.RawApiResponseService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Component
public class  FipeApiClient {

    private final RestClient restClient;
    private final RestClient restClientV2;
    private final ObjectMapper objectMapper;
    private final RawApiResponseService rawApiResponseService;
    private final int maxAttempts;
    private final Duration initialBackoff;
    private final Duration cacheTtl;
    private final Map<PrecoCacheKey, PrecoCacheEntry> cachePrecos = new ConcurrentHashMap<>();

    public FipeApiClient(RestClient.Builder restClientBuider,
                         ObjectMapper objectMapper,
                         RawApiResponseService rawApiResponseService,
                         @Value("${fipe.retry.max-attempts:3}") int maxAttempts,
                         @Value("${fipe.retry.initial-backoff:200ms}") Duration initialBackoff,
                         @Value("${fipe.cache.ttl:PT6H}") Duration cacheTtl) {
        this.restClient = restClientBuider
                .baseUrl("https://parallelum.com.br/fipe/api/v1")
                .build();
        this.restClientV2 = restClientBuider.clone()
                .baseUrl("https://fipe.parallelum.com.br/api/v2")
                .build();
        this.objectMapper = objectMapper;
        this.rawApiResponseService = rawApiResponseService;
        this.maxAttempts = maxAttempts;
        this.initialBackoff = initialBackoff;
        this.cacheTtl = cacheTtl;
    }
    public List<MarcaResponseDTO> buscarMarcas(TipoVeiculoEnum tipo, String tabelaReferencia) {
        String referencia = tabelaReferencia == null || tabelaReferencia.isBlank() ? "atual" : tabelaReferencia;
        return executarComRetry(() -> restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/{tipo}/marcas");
                    if (tabelaReferencia != null && !tabelaReferencia.isBlank()) {
                        uriBuilder.queryParam("tabela_referencia", tabelaReferencia);
                    }
                    return uriBuilder.build(tipo.getCodigoFipe());
                })
                .exchange((request, response) -> {
                    byte[] body = response.getBody().readAllBytes();
                    String chave = "marcas:" + tipo.getCodigoFipe() + ":" + referencia;
                    rawApiResponseService.registrarFipe(chave, new String(body, StandardCharsets.UTF_8),
                            response.getStatusCode().value());
                    ensureSuccessful(response.getStatusCode(), response.getHeaders(), body);
                    return objectMapper.readValue(body, new TypeReference<>() {});
                }));
    }
    public List<FipeValorDTO> buscarPorCodigo(String tipoVeiculo, String codigoFipe) {
        return executarComRetry(() -> restClient.get()
                .uri("/{tipo}/veiculos/{codigoFipe}", tipoVeiculo, codigoFipe)
                .exchange((request, response) -> {
                    byte[] body = response.getBody().readAllBytes();
                    String chave = "preco:" + tipoVeiculo + ":" + codigoFipe;
                    rawApiResponseService.registrarFipe(chave, new String(body, StandardCharsets.UTF_8),
                            response.getStatusCode().value());
                    ensureSuccessful(response.getStatusCode(), response.getHeaders(), body);
                    return objectMapper.readValue(body, new TypeReference<>() {});
                }));
    }

    public FipePrecoDTO buscarPreco(String tipoVeiculo, String codigoFipe, String codigoAno, String codigoReferencia) {
        if (codigoReferencia == null || codigoReferencia.isBlank()) {
            throw new IllegalArgumentException("A referência FIPE é obrigatória para consultar e cachear o preço.");
        }
        String tipoV2 = normalizarTipoV2(tipoVeiculo);
        PrecoCacheKey chaveCache = new PrecoCacheKey(tipoV2, codigoFipe, codigoAno, codigoReferencia);
        PrecoCacheEntry entrada = cachePrecos.get(chaveCache);
        if (entrada != null && entrada.expiraEm().isAfter(Instant.now())) {
            return entrada.preco();
        }
        if (entrada != null) {
            cachePrecos.remove(chaveCache, entrada);
        }

        FipePrecoDTO preco = executarComRetry(() -> restClientV2.get()
                .uri("/{tipo}/{codigoFipe}/years/{codigoAno}?reference={referencia}",
                        tipoV2, codigoFipe, codigoAno, codigoReferencia)
                .exchange((request, response) -> {
                    byte[] body = response.getBody().readAllBytes();
                    String chaveRaw = "preco:" + tipoV2 + ":" + codigoFipe + ":" + codigoAno + ":" + codigoReferencia;
                    rawApiResponseService.registrarFipe(chaveRaw, new String(body, StandardCharsets.UTF_8),
                            response.getStatusCode().value());
                    ensureSuccessful(response.getStatusCode(), response.getHeaders(), body);
                    return objectMapper.readValue(body, FipePrecoDTO.class);
                }));
        cachePrecos.put(chaveCache, new PrecoCacheEntry(preco, Instant.now().plus(cacheTtl)));
        return preco;
    }

    private <T> T executarComRetry(Supplier<T> chamada) {
        Duration espera = initialBackoff;
        for (int tentativa = 1; ; tentativa++) {
            try {
                return chamada.get();
            } catch (RestClientResponseException e) {
                if (e.getStatusCode().value() < 500 || tentativa >= maxAttempts) {
                    throw e;
                }
            } catch (ResourceAccessException e) {
                if (!ehTimeout(e) || tentativa >= maxAttempts) {
                    throw e;
                }
            }
            aguardar(espera);
            espera = espera.multipliedBy(2);
        }
    }

    private boolean ehTimeout(Throwable erro) {
        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
            if (causa instanceof SocketTimeoutException
                    || causa instanceof HttpTimeoutException
                    || causa instanceof TimeoutException) {
                return true;
            }
        }
        return false;
    }

    private void aguardar(Duration espera) {
        try {
            Thread.sleep(espera.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry FIPE interrompido.", e);
        }
    }

    private String normalizarTipoV2(String tipoVeiculo) {
        return switch (tipoVeiculo.trim().toLowerCase()) {
            case "carros", "cars" -> "cars";
            case "motos", "motorcycles" -> "motorcycles";
            case "caminhoes", "trucks" -> "trucks";
            default -> throw new IllegalArgumentException("Tipo de veículo FIPE inválido: " + tipoVeiculo);
        };
    }

    private void ensureSuccessful(HttpStatusCode status, HttpHeaders headers, byte[] body) {
        if (!status.is2xxSuccessful()) {
            throw new RestClientResponseException(
                    "FIPE respondeu com HTTP " + status.value(),
                    status.value(),
                    status.toString(),
                    headers,
                    body,
                    StandardCharsets.UTF_8
            );
        }
    }

    private record PrecoCacheKey(String tipoVeiculo, String codigoFipe, String codigoAno, String referencia) { }
    private record PrecoCacheEntry(FipePrecoDTO preco, Instant expiraEm) { }
}
