package com.pedrosouzza.JBLsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository

@Repository
public interface LogIngestaoRepository extends JpaRepository<LogIngestao, Long> {
}
