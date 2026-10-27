package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.client.BrasilApiClient;
import com.pedrosouzza.JBLsystem.model.RawApiResponse;
import com.pedrosouzza.JBLsystem.model.Empresa;
import com.pedrosouzza.JBLsystem.repository.EmpresaRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final BrasilApiClient brasilApiClient;
    private final RawApiResponseService rawApiResponseService;
    private final ObjectMapper objectMapper;

    public EmpresaService(EmpresaRepository empresaRepository,
                          BrasilApiClient brasilApiClient,
                          RawApiResponseService rawApiResponseService,
                          ObjectMapper objectMapper) {
        this.empresaRepository = empresaRepository;
        this.brasilApiClient = brasilApiClient;
        this.rawApiResponseService = rawApiResponseService;
        this.objectMapper = objectMapper;
    }

    public Empresa processarEBuscarEmpresa(String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("\\D", "");

        if (cnpjLimpo.length() != 14) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CNPJ deve conter 14 dígitos.");
        }

        Map<String, Object> dados = brasilApiClient.buscarCnpj(cnpjLimpo);

        return processarDadosCnpj(cnpjLimpo, dados);
    }

    public Empresa reprocessarRespostaBrutaCnpj(long rawId) {
        RawApiResponse raw = rawApiResponseService.buscarCnpj(rawId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Resposta bruta de CNPJ não encontrada: " + rawId));
        if (raw.statusHttp() < 200 || raw.statusHttp() >= 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A resposta bruta não contém um sucesso HTTP e não pode ser reprocessada.");
        }
        Map<String, Object> dados;
        try {
            dados = objectMapper.readValue(raw.jsonOriginal(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível interpretar o JSON bruto de CNPJ.", e);
        }
        return processarDadosCnpj(raw.chave(), dados);
    }

    @SuppressWarnings("unchecked")
    private Empresa processarDadosCnpj(String cnpjLimpo, Map<String, Object> dados) {
        if (dados == null || dados.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada na BrasilAPI.");
        }

        // Atualiza se já existe, cria se não existe
        Empresa empresa = empresaRepository.findByCnpj(cnpjLimpo).orElseGet(Empresa::new);
        empresa.setCnpj(cnpjLimpo);
        empresa.setRazaoSocial((String) dados.get("razao_social"));
        empresa.setPorte((String) dados.get("porte"));
        empresa.setNaturezaJuridica((String) dados.get("natureza_juridica"));
        empresa.setSituacaoCadastral((String) dados.get("descricao_situacao_cadastral"));
        empresa.setNomeFantasia((String) dados.get("nome_fantasia"));
        empresa.setMunicipio((String) dados.get("municipio"));
        empresa.setUf((String) dados.get("uf"));

        // Novos atributos com tratamento de nulos
        empresa.setCapitalSocial(converterParaBigDecimal(dados.get("capital_social")));
        empresa.setDataInicioAtividade(converterParaLocalDate(dados.get("data_inicio_atividade")));

        // Leitura da lista de CNAEs secundários
        empresa.getCnaesSecundarios().clear();
        List<Map<String, Object>> cnaesSecundariosList =
                (List<Map<String, Object>>) dados.get("cnaes_secundarios");
        if (cnaesSecundariosList != null && !cnaesSecundariosList.isEmpty()) {
            for (Map<String, Object> cnaeMap : cnaesSecundariosList) {
                Integer codigo = cnaeMap.get("codigo") != null
                        ? ((Number) cnaeMap.get("codigo")).intValue()
                        : null;
                String descricao = (String) cnaeMap.get("descricao");

                if (codigo != null) {
                    empresa.addCnaeSecundario(codigo, descricao);
                }
            }
        }

        return empresaRepository.save(empresa);
    }

    private BigDecimal converterParaBigDecimal(Object valor) {
        if (valor == null) return null;
        if (valor instanceof Number) {
            return BigDecimal.valueOf(((Number) valor).doubleValue());
        }
        try {
            return new BigDecimal(valor.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate converterParaLocalDate(Object valor) {
        if (valor == null) return null;
        try {
            return LocalDate.parse(valor.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
