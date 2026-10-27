package com.pedrosouzza.JBLsystem.repository;

import com.pedrosouzza.JBLsystem.model.VeiculoFipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VeiculoFipeRepository extends JpaRepository<VeiculoFipe, String> {
}
