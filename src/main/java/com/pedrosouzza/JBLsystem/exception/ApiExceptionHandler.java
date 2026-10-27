package com.pedrosouzza.JBLsystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final ObjectMapper objectMapper;

    public ApiExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiErrorResponse> tratarErroDaApiExterna(
            RestClientResponseException exception,
            HttpServletRequest request) {
        HttpStatusCode status = exception.getStatusCode();
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                "Erro retornado pela API externa",
                extrairMensagem(exception),
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiErrorResponse> tratarFalhaDeConexao(
            ResourceAccessException exception,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiErrorResponse(
                Instant.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "API externa indisponível",
                "Não foi possível concluir a chamada à API externa após as tentativas configuradas.",
                request.getRequestURI()
        ));
    }

    private String extrairMensagem(RestClientResponseException exception) {
        String corpo = exception.getResponseBodyAsString();
        if (corpo == null || corpo.isBlank()) {
            return "A API externa respondeu com HTTP " + exception.getStatusCode().value() + ".";
        }

        try {
            JsonNode json = objectMapper.readTree(corpo);
            JsonNode erro = json == null ? null : json.get("error");
            if (erro != null && erro.isTextual()) {
                return erro.stringValue();
            }
        } catch (Exception ignored) {
            // Se a resposta não for JSON, devolvemos o corpo original abaixo.
        }
        return corpo;
    }

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
    }
}
