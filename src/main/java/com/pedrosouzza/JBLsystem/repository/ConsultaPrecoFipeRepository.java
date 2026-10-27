package com.pedrosouzza.JBLsystem.repository;

import com.pedrosouzza.JBLsystem.model.ConsultaPrecoFipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsultaPrecoFipeRepository extends JpaRepository<ConsultaPrecoFipe, Long> {
    Optional<ConsultaPrecoFipe> findByTipoVeiculoAndCodigoFipeAndCodigoAnoAndCodigoReferencia(
            String tipoVeiculo, String codigoFipe, String codigoAno, String codigoReferencia);
}
