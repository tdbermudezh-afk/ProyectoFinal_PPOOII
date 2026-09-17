package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByIdentificacion(String identificacion);

    List<Persona> findByTipoPersona(String tipoPersona);

    boolean existsByIdentificacion(String identificacion);

    boolean existsByCorreo(String correo);
}