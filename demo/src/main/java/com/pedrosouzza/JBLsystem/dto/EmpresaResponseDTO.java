package com.pedrosouzza.JBLsystem.dto;

import com.pedrosouzza.JBLsystem.model.Empresa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record EmpresaResponseDTO(
        String cnpj,
        String razaoSocial,
        String nomeFantasia,
        String porte,
        String naturezaJuridica,
        String situacaoCadastral,
        String municipio,
        String uf,
        BigDecimal capitalSocial,
        LocalDate dataInicioAtividade,
        List<CnaeDTO> cnaesSecundarios
) {
    public record CnaeDTO(Integer codigo, String descricao) {}

    public static EmpresaResponseDTO fromEntity(Empresa empresa) {
        List<CnaeDTO> cnaes = empresa.getCnaesSecundarios().stream()
                .map(cnae -> new CnaeDTO(cnae.getCodigoCnae(), cnae.getDescricaoCnae()))
                .collect(Collectors.toList());

        return new EmpresaResponseDTO(
                empresa.getCnpj(),
                empresa.getRazaoSocial(),
                empresa.getNomeFantasia(),
                empresa.getPorte(),
                empresa.getNaturezaJuridica(),
                empresa.getSituacaoCadastral(),
                empresa.getMunicipio(),
                empresa.getUf(),
                empresa.getCapitalSocial(),
                empresa.getDataInicioAtividade(),
                cnaes
        );
    }
}