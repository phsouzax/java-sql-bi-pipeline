package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.client.BrasilApiClient;
import com.pedrosouzza.JBLsystem.dto.BrasilApiCnpjDto;
import com.pedrosouzza.JBLsystem.model.Empresa;
import com.pedrosouzza.JBLsystem.model.LogIngestao;
import com.pedrosouzza.JBLsystem.repository.EmpresaRepository;
import com.pedrosouzza.JBLsystem.repository.LogIngestaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {

    private final BrasilApiClient brasilApiClient;
    private final EmpresaRepository empresaRepository;
    private final LogIngestaoRepository logIngestaoRepository;

    public EmpresaService(BrasilApiClient brasilApiClient,
                          EmpresaRepository empresaRepository,
                          LogIngestaoRepository logIngestaoRepository) {
        this.brasilApiClient = brasilApiClient;
        this.empresaRepository = empresaRepository;
        this.logIngestaoRepository = logIngestaoRepository;
    }

    @Transactional
    public Empresa processarEBuscarEmpresa(String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("\\D", "");

        try {
            // 1. Busca dados atualizados da empresa na BrasilAPI
            BrasilApiCnpjDto dto = brasilApiClient.buscarCnpj(cnpjLimpo);

            // 2. Lógica de Upsert: se já existe no banco MySQL, atualiza a razão social; caso contrário, cria um novo registro
            Empresa empresa = empresaRepository.findByCnpj(cnpjLimpo)
                    .map(existente -> {
                        existente.setRazaoSocial(dto.razaoSocial());
                        return existente;
                    })
                    .orElseGet(() -> new Empresa(cnpjLimpo, dto.razaoSocial()));

            Empresa empresaSalva = empresaRepository.save(empresa);

            // 3. Registra o log de sucesso
            registrarLog("BrasilAPI - CNPJ", "SUCESSO", 1, null);

            return empresaSalva;

        } catch (Exception e) {
            // Registra o log em caso de erro na consulta/ingestão
            registrarLog("BrasilAPI - CNPJ", "ERRO", 0, e.getMessage());
            throw new RuntimeException("Falha ao processar CNPJ: " + e.getMessage(), e);
        }
    }

    private void registrarLog(String api, String status, Integer registros, String erro) {
        LogIngestao log = new LogIngestao();
        log.setNomeApi(api);
        log.setStatus(status);
        log.setRegistrosProcessados(registros);
        log.setMensagemErro(erro);
        logIngestaoRepository.save(log);
    }
}
