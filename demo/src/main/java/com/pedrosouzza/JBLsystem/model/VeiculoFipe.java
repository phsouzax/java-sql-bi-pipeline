package com.pedrosouzza.JBLsystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "veiculo_fipe")
public class VeiculoFipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_fipe", nullable = false)
    private String codigoFipe;

    private String marca;
    private String modelo;

    @Column(name = "ano_modelo")
    private Integer anoModelo;

    private BigDecimal valor;

    @Column(name = "mes_referencia")
    private String mesReferencia;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public VeiculoFipe() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoFipe() { return codigoFipe; }
    public void setCodigoFipe(String codigoFipe) { this.codigoFipe = codigoFipe; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getAnoModelo() { return anoModelo; }
    public void setAnoModelo(Integer anoModelo) { this.anoModelo = anoModelo; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getMesReferencia() { return mesReferencia; }
    public void setMesReferencia(String mesReferencia) { this.mesReferencia = mesReferencia; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}