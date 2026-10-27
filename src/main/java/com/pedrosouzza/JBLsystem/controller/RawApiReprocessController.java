package com.pedrosouzza.JBLsystem.controller;

import com.pedrosouzza.JBLsystem.dto.EmpresaResponseDTO;
import com.pedrosouzza.JBLsystem.dto.FipePrecoDTO;
import com.pedrosouzza.JBLsystem.service.AtualizacaoPrecoFipeJob;
import com.pedrosouzza.JBLsystem.service.EmpresaService;
import com.pedrosouzza.JBLsystem.service.FipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs/raw")
public class RawApiReprocessController {

    private final EmpresaService empresaService;
    private final AtualizacaoPrecoFipeJob atualizacaoPrecoFipeJob;
    private final FipeService fipeService;

    public RawApiReprocessController(EmpresaService empresaService,
                                    AtualizacaoPrecoFipeJob atualizacaoPrecoFipeJob,
                                    FipeService fipeService) {
        this.empresaService = empresaService;
        this.atualizacaoPrecoFipeJob = atualizacaoPrecoFipeJob;
        this.fipeService = fipeService;
    }

    @PostMapping("/cnpj/{id}/reprocessar")
    public ResponseEntity<EmpresaResponseDTO> reprocessarCnpj(@PathVariable long id) {
        return ResponseEntity.ok(EmpresaResponseDTO.fromEntity(empresaService.reprocessarRespostaBrutaCnpj(id)));
    }

    @PostMapping("/fipe/{id}/reprocessar")
    public ResponseEntity<AtualizacaoPrecoFipeJob.Resultado> reprocessarFipe(@PathVariable long id) {
        AtualizacaoPrecoFipeJob.Resultado resultado = atualizacaoPrecoFipeJob.reprocessarRespostaBruta(id);
        HttpStatus status = "SUCESSO".equals(resultado.status()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(resultado);
    }

    @PostMapping("/fipe-consulta/{id}/reprocessar")
    public ResponseEntity<FipePrecoDTO> reprocessarConsultaFipe(@PathVariable long id) {
        return ResponseEntity.ok(fipeService.reprocessarConsultaBruta(id));
    }
}
