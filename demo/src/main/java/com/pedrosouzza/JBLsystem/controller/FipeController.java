package com.pedrosouzza.JBLsystem.controller;

import com.pedrosouzza.JBLsystem.dto.MarcaResponseDTO;
import com.pedrosouzza.JBLsystem.service.FipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fipe")
public class FipeController {
     private final FipeService fipeService;

     public FipeController(FipeService fipeService) {
         this.fipeService = fipeService;
     }
    @GetMapping("/{tipo}/marcas")
    public ResponseEntity<List<MarcaResponseDTO>> listarMarcas(
            @PathVariable String tipo,
            @RequestParam(required = false) String tabelaReferencia,
            @RequestParam(required = false) String nome) {

        List<MarcaResponseDTO> marcas = fipeService.listarMarcas(tipo, tabelaReferencia, nome);
        return ResponseEntity.ok(marcas);
    }
}
