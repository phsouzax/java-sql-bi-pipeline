package com.pedrosouzza.JBLsystem.controller;

import com.pedrosouzza.JBLsystem.service.FatoPrecoFipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/fipe/historico")
public class FatoPrecoFipeController {

    private final FatoPrecoFipeService fatoPrecoFipeService;

    public FatoPrecoFipeController(FatoPrecoFipeService fatoPrecoFipeService) {
        this.fatoPrecoFipeService = fatoPrecoFipeService;
    }

    @PostMapping
    public ResponseEntity<String> salvarHistorico(
            @RequestParam Long veiculoId,
            @RequestParam String mesReferencia,
            @RequestParam BigDecimal valor) {

        fatoPrecoFipeService.salvarHistoricoPreco(veiculoId, mesReferencia, valor);
        return ResponseEntity.ok("Histórico de preço processado com sucesso!");
    }
}