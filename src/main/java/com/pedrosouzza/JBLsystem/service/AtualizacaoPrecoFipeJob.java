package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.client.FipeApiClient;
import com.pedrosouzza.JBLsystem.dto.FipeValorDTO;
import com.pedrosouzza.JBLsystem.dto.FipePrecoDTO;
import com.pedrosouzza.JBLsystem.model.RawApiResponse;
import com.pedrosouzza.JBLsystem.model.LogIngestao;
import com.pedrosouzza.JBLsystem.model.VeiculoFipe;
import com.pedrosouzza.JBLsystem.repository.LogIngestaoRepository;
import com.pedrosouzza.JBLsystem.repository.VeiculoFipeRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AtualizacaoPrecoFipeJob {
    private static final Logger log = LoggerFactory.getLogger(AtualizacaoPrecoFipeJob.class);
    private static final DateTimeFormatter MES_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    private final VeiculoFipeRepository veiculoRepository;
    private final FipeApiClient fipeApiClient;
    private final FatoPrecoFipeService historicoService;
    private final LogIngestaoRepository logRepository;
    private final RawApiResponseService rawApiResponseService;
    private final ObjectMapper objectMapper;
    private final String tipoVeiculo;

    public AtualizacaoPrecoFipeJob(VeiculoFipeRepository veiculoRepository,
                                  FipeApiClient fipeApiClient,
                                  FatoPrecoFipeService historicoService,
                                  LogIngestaoRepository logRepository,
                                  RawApiResponseService rawApiResponseService,
                                  ObjectMapper objectMapper,
                                  @Value("${fipe.tipo-veiculo:${FIPE_TIPO_VEICULO:carros}}") String tipoVeiculo) {
        this.veiculoRepository = veiculoRepository;
        this.fipeApiClient = fipeApiClient;
        this.historicoService = historicoService;
        this.logRepository = logRepository;
        this.rawApiResponseService = rawApiResponseService;
        this.objectMapper = objectMapper;
        this.tipoVeiculo = tipoVeiculo;
    }

    @Scheduled(cron = "${jobs.fipe-precos.cron:${FIPE_PRECOS_CRON:0 0 2 1 * *}}",
            zone = "${jobs.fipe-precos.time-zone:${FIPE_PRECOS_TIME_ZONE:America/Sao_Paulo}}")
    @Transactional
    public void executarAgendado() {
        executar("AUTOMATICA");
    }

    @Transactional
    public Resultado executarManualmente() {
        return executar("MANUAL");
    }

    @Transactional
    public Resultado executar(String tipoIngestao) {
        LocalDateTime inicio = LocalDateTime.now();
        int atualizados = 0;
        try {
            List<VeiculoFipe> veiculos = veiculoRepository.findAll();
            for (VeiculoFipe veiculo : veiculos) {
                List<FipeValorDTO> precos = fipeApiClient.buscarPorCodigo(tipoVeiculo, veiculo.getCodigoFipe());
                if (precos == null || precos.isEmpty()) {
                    throw new IllegalStateException("A FIPE não retornou preço para " + veiculo.getCodigoFipe());
                }

                FipeValorDTO preco = precos.get(0);
                if (preco.getValorFipe() == null) {
                    throw new IllegalStateException("Preço FIPE vazio para " + veiculo.getCodigoFipe());
                }
                salvarPreco(veiculo, preco);
                atualizados++;
            }

            logRepository.save(new LogIngestao(tipoIngestao, "SUCESSO",
                    "Códigos processados: " + atualizados, inicio));
            return new Resultado("SUCESSO", atualizados, "Atualização FIPE concluída.");
        } catch (Exception e) {
            log.error("Falha na atualização de preços FIPE", e);
            logRepository.save(new LogIngestao(tipoIngestao, "ERRO", limitar(e.getMessage()), inicio));
            return new Resultado("ERRO", atualizados, "Falha ao atualizar preços FIPE: " + limitar(e.getMessage()));
        }
    }

    @Transactional
    public Resultado reprocessarRespostaBruta(long rawId) {
        LocalDateTime inicio = LocalDateTime.now();
        try {
            RawApiResponse raw = rawApiResponseService.buscarFipe(rawId)
                    .orElseThrow(() -> new IllegalArgumentException("Resposta bruta FIPE não encontrada: " + rawId));
            if (raw.statusHttp() < 200 || raw.statusHttp() >= 300) {
                throw new IllegalArgumentException("A resposta bruta não contém um sucesso HTTP.");
            }
            String[] chave = raw.chave().split(":");
            if (chave.length != 3 || !"preco".equals(chave[0])) {
                throw new IllegalArgumentException("A resposta bruta não é uma consulta de preço FIPE.");
            }
            String codigoFipe = chave[2];

            FipeValorDTO preco = extrairPreco(raw.jsonOriginal());
            VeiculoFipe veiculo = veiculoRepository.findById(codigoFipe)
                    .orElseThrow(() -> new IllegalArgumentException("Veículo FIPE não encontrado: " + codigoFipe));
            if (preco.getValorFipe() == null) {
                throw new IllegalArgumentException("A resposta bruta FIPE não contém valor.");
            }

            salvarPreco(veiculo, preco);
            logRepository.save(new LogIngestao("REPROCESSAMENTO_RAW", "SUCESSO",
                    "Resposta FIPE bruta reprocessada: " + rawId, inicio));
            return new Resultado("SUCESSO", 1, "Resposta FIPE bruta reprocessada sem chamada externa.");
        } catch (Exception e) {
            log.error("Falha ao reprocessar resposta bruta FIPE {}", rawId, e);
            logRepository.save(new LogIngestao("REPROCESSAMENTO_RAW", "ERRO", limitar(e.getMessage()), inicio));
            return new Resultado("ERRO", 0, "Falha ao reprocessar resposta FIPE bruta: " + limitar(e.getMessage()));
        }
    }

    private void salvarPreco(VeiculoFipe veiculo, FipeValorDTO preco) {
        String mesReferencia = normalizarMes(preco.getMesReferencia());
        veiculo.setValor(preco.getValorFipe());
        veiculo.setMesReferencia(mesReferencia);
        veiculo.setDataAtualizacao(LocalDateTime.now());
        veiculoRepository.save(veiculo);
        historicoService.salvarHistoricoPreco(veiculo.getCodigoFipe(), mesReferencia, preco.getValorFipe());
    }

    private FipeValorDTO extrairPreco(String json) throws Exception {
        JsonNode raiz = objectMapper.readTree(json);
        if (raiz.isArray()) {
            List<FipeValorDTO> precos = objectMapper.readValue(json, new TypeReference<>() {});
            if (precos == null || precos.isEmpty()) {
                throw new IllegalArgumentException("A resposta bruta FIPE não contém preços.");
            }
            return precos.get(0);
        }

        FipePrecoDTO preco = objectMapper.treeToValue(raiz, FipePrecoDTO.class);
        if (preco.price() == null || preco.price().isBlank()) {
            throw new IllegalArgumentException("A resposta bruta FIPE não contém preço.");
        }
        String valorNormalizado = preco.price().replace("R$", "").replace(".", "").replace(",", ".").trim();
        return FipeValorDTO.builder()
                .codigoFipe(preco.codeFipe())
                .valorFipe(new java.math.BigDecimal(valorNormalizado))
                .mesReferencia(preco.referenceMonth())
                .build();
    }

    private String normalizarMes(String mes) {
        if (mes == null || mes.isBlank()) {
            return YearMonth.now().format(MES_FORMATTER);
        }
        String valor = mes.trim();
        try {
            return YearMonth.parse(valor).format(MES_FORMATTER);
        } catch (Exception ignored) {
            return valor;
        }
    }

    private String limitar(String mensagem) {
        if (mensagem == null || mensagem.isBlank()) return "Erro sem mensagem.";
        return mensagem.length() > 2000 ? mensagem.substring(0, 2000) : mensagem;
    }

    public record Resultado(String status, int codigosProcessados, String mensagem) { }
}
