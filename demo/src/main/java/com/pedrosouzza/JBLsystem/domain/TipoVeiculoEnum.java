package com.pedrosouzza.JBLsystem.domain;

public enum TipoVeiculoEnum {

    CARROS("carros"),
    MOTOS("motos"),
    CAMINHOES("caminhoes");

    private final String codigoFipe;

    TipoVeiculoEnum(String codigoFipe) {
        this.codigoFipe = codigoFipe;
    }

    public String getCodigoFipe() {
        return codigoFipe;
    }

    public static TipoVeiculoEnum fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Tipo de veículo não pode ser nulo ou vazio.");
        }

        try {
            return TipoVeiculoEnum.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de veículo inválido: " + valor);
        }
    }
}