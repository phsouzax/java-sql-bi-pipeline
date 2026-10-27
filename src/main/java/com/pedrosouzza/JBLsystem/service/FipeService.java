package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.client.FipeApiClient;
import com.pedrosouzza.JBLsystem.domain.FatoPrecoFipe;
import com.pedrosouzza.JBLsystem.domain.TipoVeiculoEnum;
import com.pedrosouzza.JBLsystem.dto.FipeValorDTO;
import com.pedrosouzza.JBLsystem.dto.FipePrecoDTO;
import com.pedrosouzza.JBLsystem.dto.MarcaResponseDTO;
import com.pedrosouzza.JBLsystem.exception.ResourceNotFoundException;
import com.pedrosouzza.JBLsystem.repository.FatoPrecoFipeRepository;
import com.pedrosouzza.JBLsystem.model.ConsultaPrecoFipe;
import com.pedrosouzza.JBLsystem.model.RawApiResponse;
import com.pedrosouzza.JBLsystem.repository.ConsultaPrecoFipeRepository;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class FipeService {

    private final FipeApiClient fipeApiClient;
    private final FatoPrecoFipeRepository fatoPrecoFipeRepository;
    private final ConsultaPrecoFipeRepository consultaPrecoFipeRepository;
    private final RawApiResponseService rawApiResponseService;
    private final ObjectMapper objectMapper;
    private final Duration cacheTtl;

    public FipeService(FipeApiClient fipeApiClient,
                       FatoPrecoFipeRepository fatoPrecoFipeRepository,
                       ConsultaPrecoFipeRepository consultaPrecoFipeRepository,
                       RawApiResponseService rawApiResponseService,
                       ObjectMapper objectMapper,
                       @Value("${fipe.cache.ttl:PT6H}") Duration cacheTtl) {
        this.fipeApiClient = fipeApiClient;
        this.fatoPrecoFipeRepository = fatoPrecoFipeRepository;
        this.consultaPrecoFipeRepository = consultaPrecoFipeRepository;
        this.rawApiResponseService = rawApiResponseService;
        this.objectMapper = objectMapper;
        this.cacheTtl = cacheTtl;
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

    public FipePrecoDTO buscarPreco(String tipoVeiculo, String codigoFipe, String codigoAno, String referencia) {
        String tipoNormalizado = normalizarTipoVeiculo(tipoVeiculo);
        Optional<ConsultaPrecoFipe> cachePersistente = consultaPrecoFipeRepository
                .findByTipoVeiculoAndCodigoFipeAndCodigoAnoAndCodigoReferencia(
                        tipoNormalizado, codigoFipe, codigoAno, referencia);
        LocalDateTime agora = LocalDateTime.now();
        if (cachePersistente.isPresent()
                && cachePersistente.get().getConsultadoEm().plus(cacheTtl).isAfter(agora)) {
            return cachePersistente.get().toDto();
        }

        FipePrecoDTO preco = fipeApiClient.buscarPreco(tipoNormalizado, codigoFipe, codigoAno, referencia);
        salvarConsultaPontual(tipoNormalizado, codigoFipe, codigoAno, referencia, preco, agora);
        return preco;
    }

    public FipePrecoDTO reprocessarConsultaBruta(long rawId) {
        RawApiResponse raw = rawApiResponseService.buscarFipe(rawId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Resposta bruta FIPE não encontrada: " + rawId));
        if (raw.statusHttp() < 200 || raw.statusHttp() >= 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A resposta bruta não contém um sucesso HTTP e não pode ser reprocessada.");
        }
        String[] chave = raw.chave().split(":");
        if (chave.length != 5 || !"preco".equals(chave[0])) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A resposta bruta não é uma consulta pontual de preço FIPE.");
        }

        FipePrecoDTO preco;
        try {
            preco = objectMapper.readValue(raw.jsonOriginal(), FipePrecoDTO.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível interpretar o JSON bruto de preço FIPE.", e);
        }
        salvarConsultaPontual(chave[1], chave[2], chave[3], chave[4], preco, raw.dataChamada());
        return preco;
    }

    private ConsultaPrecoFipe salvarConsultaPontual(String tipoVeiculo, String codigoFipe, String codigoAno,
                                                    String referencia, FipePrecoDTO preco, LocalDateTime consultadoEm) {
        Optional<ConsultaPrecoFipe> registroExistente = consultaPrecoFipeRepository
                .findByTipoVeiculoAndCodigoFipeAndCodigoAnoAndCodigoReferencia(
                        tipoVeiculo, codigoFipe, codigoAno, referencia);
        ConsultaPrecoFipe consulta = registroExistente.orElseGet(() ->
                new ConsultaPrecoFipe(tipoVeiculo, codigoFipe, codigoAno, referencia, preco, consultadoEm));
        if (registroExistente.isPresent()) {
            consulta.atualizar(preco, consultadoEm);
        }
        return consultaPrecoFipeRepository.save(consulta);
    }

    private String normalizarTipoVeiculo(String tipoVeiculo) {
        return switch (tipoVeiculo.trim().toLowerCase()) {
            case "carros", "cars" -> "cars";
            case "motos", "motorcycles" -> "motorcycles";
            case "caminhoes", "trucks" -> "trucks";
            default -> throw new IllegalArgumentException("Tipo de veículo FIPE inválido: " + tipoVeiculo);
        };
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
