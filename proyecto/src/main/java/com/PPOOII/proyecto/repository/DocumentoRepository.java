// Paquete de acceso a datos (Repositorios JPA)
package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Interfaz de repositorio Spring Data JPA para la entidad paramétrica Documento
@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Integer> {

    // Consulta de existencia: Verifica si ya existe un tipo de documento con el código dado
    boolean existsByCodigo(String codigo);

    // Consulta derivada: Permite buscar un documento a través de su código parametrizado
    Optional<Documento> findByCodigo(String codigo);
}