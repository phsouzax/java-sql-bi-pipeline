package com.pedrosouzza.JBLsystem.service;

import com.pedrosouzza.JBLsystem.domain.FatoPrecoFipe;
import com.pedrosouzza.JBLsystem.repository.FatoPrecoFipeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class FatoPrecoFipeService {

    private final FatoPrecoFipeRepository fatoPrecoFipeRepository;

    public FatoPrecoFipeService(FatoPrecoFipeRepository fatoPrecoFipeRepository) {
        this.fatoPrecoFipeRepository = fatoPrecoFipeRepository;
    }

    public void salvarHistoricoPreco(Long veiculoId, String mesReferencia, BigDecimal valor) {
        boolean jaExiste = fatoPrecoFipeRepository.findByVeiculoIdAndMesReferencia(veiculoId, mesReferencia).isPresent();

        if (!jaExiste) {
            FatoPrecoFipe historico = new FatoPrecoFipe(null, veiculoId, mesReferencia, valor, LocalDateTime.now());

            fatoPrecoFipeRepository.save(historico);
        }
    }
}