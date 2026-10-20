package com.pedrosouzza.JBLsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FipeValorDTO {
    private String codigoFipe;
    private BigDecimal valorFipe;
    private LocalDate mesReferencia;

}