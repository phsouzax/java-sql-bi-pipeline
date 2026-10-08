package com.pedrosouzza.JBLsystem.controller;

import com.pedrosouzza.JBLsystem.model.Empresa;
import com.pedrosouzza.JBLsystem.service.EmpresaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping("/ingestao/{cnpj}")
    public ResponseEntity<Empresa> ingerirEmpresa(@PathVariable String cnpj) {
        Empresa empresaSalva = empresaService.processarEBuscarEmpresa(cnpj);
        return ResponseEntity.ok(empresaSalva);
    }
}