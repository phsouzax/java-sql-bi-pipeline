package com.pedrosouzza.JBLsystem.repository;

import com.pedrosouzza.JBLsystem.model.LogIngestao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogIngestaoRepository extends JpaRepository<LogIngestao, Long> {
}