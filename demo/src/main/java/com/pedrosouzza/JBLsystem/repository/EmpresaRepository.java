package com.pedrosouzza.JBLsystem.repository;

import com.pedrosouzza.JBLsystem.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByCnpj (String cnpj);
}
