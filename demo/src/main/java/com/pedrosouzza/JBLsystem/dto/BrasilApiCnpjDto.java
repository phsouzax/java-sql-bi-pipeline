package com.pedrosouzza.JBLsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BrasilApiCnpjDto (
    String cnpj,
    @JsonProperty("razao_social") String razaoSocial,
    @JsonProperty("nome_fantasia") String nomeFantasia,
    @JsonProperty("cnae_fiscal") String cnaeFiscal,
    @JsonProperty("cnae_fiscal_descricao") String cnaeFiscalDescricao,
    @JsonProperty("descricao_porte") String descricaoPorte,
    String uf,
    String municipio

) {}
