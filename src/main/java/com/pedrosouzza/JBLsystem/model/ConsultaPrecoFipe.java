package com.pedrosouzza.JBLsystem.model;

import com.pedrosouzza.JBLsystem.dto.FipePrecoDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "consulta_preco_fipe", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tipo_veiculo", "codigo_fipe", "codigo_ano", "codigo_referencia"})
})
public class ConsultaPrecoFipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_veiculo", nullable = false, length = 20)
    private String tipoVeiculo;

    @Column(name = "codigo_fipe", nullable = false, length = 20)
    private String codigoFipe;

    @Column(name = "codigo_ano", nullable = false, length = 20)
    private String codigoAno;

    @Column(name = "codigo_referencia", nullable = false, length = 10)
    private String codigoReferencia;

    @Column(nullable = false, length = 120)
    private String marca;

    @Column(nullable = false, length = 255)
    private String modelo;

    @Column(length = 60)
    private String combustivel;

    @Column(name = "sigla_combustivel", length = 5)
    private String siglaCombustivel;

    @Column(name = "ano_modelo")
    private Integer anoModelo;

    @Column(name = "codigo_tipo_fipe")
    private Integer codigoTipoFipe;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "mes_referencia", nullable = false, length = 50)
    private String mesReferencia;

    @Column(name = "consultado_em", nullable = false)
    private LocalDateTime consultadoEm;

    protected ConsultaPrecoFipe() {
    }

    public ConsultaPrecoFipe(String tipoVeiculo, String codigoFipe, String codigoAno, String codigoReferencia,
                             FipePrecoDTO preco, LocalDateTime consultadoEm) {
        this.tipoVeiculo = tipoVeiculo;
        this.codigoFipe = codigoFipe;
        this.codigoAno = codigoAno;
        this.codigoReferencia = codigoReferencia;
        atualizar(preco, consultadoEm);
    }

    public void atualizar(FipePrecoDTO preco, LocalDateTime consultadoEm) {
        this.marca = preco.brand();
        this.modelo = preco.model();
        this.combustivel = preco.fuel();
        this.siglaCombustivel = preco.fuelAcronym();
        this.anoModelo = preco.modelYear();
        this.codigoTipoFipe = preco.vehicleType();
        this.valor = converterValor(preco.price());
        this.mesReferencia = preco.referenceMonth();
        this.consultadoEm = consultadoEm;
    }

    public FipePrecoDTO toDto() {
        NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        return new FipePrecoDTO(marca, codigoFipe, combustivel, siglaCombustivel, modelo,
                anoModelo, moeda.format(valor), mesReferencia, codigoTipoFipe);
    }

    private BigDecimal converterValor(String preco) {
        String normalizado = preco.replace("R$", "").replace(".", "").replace(",", ".").trim();
        return new BigDecimal(normalizado);
    }

    public LocalDateTime getConsultadoEm() {
        return consultadoEm;
    }
}
