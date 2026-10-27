package com.pedrosouzza.JBLsystem.model;

import java.time.LocalDateTime;

public record RawApiResponse(
        Long id,
        String chave,
        String jsonOriginal,
        LocalDateTime dataChamada,
        int statusHttp
) {
}
