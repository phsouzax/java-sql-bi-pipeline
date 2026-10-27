package com.pedrosouzza.JBLsystem.dto;

public record FipePrecoDTO(
        String brand,
        String codeFipe,
        String fuel,
        String fuelAcronym,
        String model,
        Integer modelYear,
        String price,
        String referenceMonth,
        Integer vehicleType
) {
}
