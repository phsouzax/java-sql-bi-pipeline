package com.pedrosouzza.JBLsystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fato_preco_fipe", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"codigo_fipe", "mes_referencia"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FatoPrecoFipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_fipe", nullable = false, length = 20)
    private String codigoFipe;

    @Column(name = "mes_referencia", nullable = false)
    private String mesReferencia;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
}
