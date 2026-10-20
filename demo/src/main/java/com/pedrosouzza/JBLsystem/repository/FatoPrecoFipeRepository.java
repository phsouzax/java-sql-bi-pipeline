package com.pedrosouzza.JBLsystem.repository;

import com.pedrosouzza.JBLsystem.domain.FatoPrecoFipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FatoPrecoFipeRepository extends JpaRepository<FatoPrecoFipe, Long> {

    Optional<FatoPrecoFipe> findByVeiculoIdAndMesReferencia (Long veiculoId, String mesReferencia);
}
