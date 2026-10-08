package com.pedrosouzza.JBLsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "dim_empresa_cnae")
public class EmpresaCnae {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cnpj_empresa", nullable = false)
    @JsonIgnore // Evita recuo infinito na serialização JSON
    private Empresa empresa;

    private Integer codigoCnae;
    private String descricaoCnae;

    public EmpresaCnae() {}

    public EmpresaCnae(Empresa empresa, Integer codigoCnae, String descricaoCnae) {
        this.empresa = empresa;
        this.codigoCnae = codigoCnae;
        this.descricaoCnae = descricaoCnae;
    }

    public Long getId() { return id; }

    public Empresa getEmpresa() { return empresa; }

    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public Integer getCodigoCnae() { return codigoCnae; }

    public void setCodigoCnae(Integer codigoCnae) { this.codigoCnae = codigoCnae; }

    public String getDescricaoCnae() { return descricaoCnae; }

    public void setDescricaoCnae(String descricaoCnae) { this.descricaoCnae = descricaoCnae; }
}