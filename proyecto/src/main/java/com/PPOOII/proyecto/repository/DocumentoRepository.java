package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Integer> {
    boolean existsByCodigo(String codigo);
    java.util.Optional<Documento> findByCodigo(String codigo);
}