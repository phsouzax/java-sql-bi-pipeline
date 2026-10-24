package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.client.FipeApiClient;
import com.pedrosouzza.JBLsystem.dto.FipeValorDTO;
import com.pedrosouzza.JBLsystem.model.LogIngestao;
import com.pedrosouzza.JBLsystem.model.VeiculoFipe;
import com.pedrosouzza.JBLsystem.repository.LogIngestaoRepository;
import com.pedrosouzza.JBLsystem.repository.VeiculoFipeRepository;
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
    private final String tipoVeiculo;

    public AtualizacaoPrecoFipeJob(VeiculoFipeRepository veiculoRepository,
                                  FipeApiClient fipeApiClient,
                                  FatoPrecoFipeService historicoService,
                                  LogIngestaoRepository logRepository,
                                  @Value("${fipe.tipo-veiculo:${FIPE_TIPO_VEICULO:carros}}") String tipoVeiculo) {
        this.veiculoRepository = veiculoRepository;
        this.fipeApiClient = fipeApiClient;
        this.historicoService = historicoService;
        this.logRepository = logRepository;
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
                String mesReferencia = normalizarMes(preco.getMesReferencia());
                veiculo.setValor(preco.getValorFipe());
                veiculo.setMesReferencia(mesReferencia);
                veiculo.setDataAtualizacao(LocalDateTime.now());
                veiculoRepository.save(veiculo);
                historicoService.salvarHistoricoPreco(veiculo.getCodigoFipe(), mesReferencia, preco.getValorFipe());
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
