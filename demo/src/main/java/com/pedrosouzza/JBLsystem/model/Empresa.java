package com.pedrosouzza.JBLsystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    private String cnpj;
    private String razaoSocial;
    private String nomeFantasia;
    private String porte;
    private String naturezaJuridica;
    private String situacaoCadastral;
    private String municipio;
    private String uf;
    private BigDecimal capitalSocial;
    private LocalDate dataInicioAtividade;

    @Column(name = "data_criacao", insertable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmpresaCnae> cnaesSecundarios = new ArrayList<>();

    public Empresa() {}

    public void addCnaeSecundario(Integer codigo, String descricao) {
        EmpresaCnae cnae = new EmpresaCnae(this, codigo, descricao);
        this.cnaesSecundarios.add(cnae);
    }

    // Getters e Setters
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }
    public String getPorte() { return porte; }
    public void setPorte(String porte) { this.porte = porte; }
    public String getNaturezaJuridica() { return naturezaJuridica; }
    public void setNaturezaJuridica(String naturezaJuridica) { this.naturezaJuridica = naturezaJuridica; }
    public String getSituacaoCadastral() { return situacaoCadastral; }
    public void setSituacaoCadastral(String situacaoCadastral) { this.situacaoCadastral = situacaoCadastral; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public BigDecimal getCapitalSocial() { return capitalSocial; }
    public void setCapitalSocial(BigDecimal capitalSocial) { this.capitalSocial = capitalSocial; }
    public LocalDate getDataInicioAtividade() { return dataInicioAtividade; }
    public void setDataInicioAtividade(LocalDate dataInicioAtividade) { this.dataInicioAtividade = dataInicioAtividade; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public List<EmpresaCnae> getCnaesSecundarios() { return cnaesSecundarios; }
    public void setCnaesSecundarios(List<EmpresaCnae> cnaesSecundarios) { this.cnaesSecundarios = cnaesSecundarios; }
}