package com.pedrosouzza.JBLsystem.controller;

import com.pedrosouzza.JBLsystem.service.AtualizacaoPrecoFipeJob;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs/fipe-precos")
public class AtualizacaoPrecoFipeController {
    private final AtualizacaoPrecoFipeJob job;

    public AtualizacaoPrecoFipeController(AtualizacaoPrecoFipeJob job) {
        this.job = job;
    }

    @PostMapping("/executar")
    public ResponseEntity<AtualizacaoPrecoFipeJob.Resultado> executar() {
        AtualizacaoPrecoFipeJob.Resultado resultado = job.executarManualmente();
        HttpStatus status = "SUCESSO".equals(resultado.status()) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(resultado);
    }
}
