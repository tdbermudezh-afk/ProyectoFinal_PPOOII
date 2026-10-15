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

    @org.springframework.data.jpa.repository.Query("SELECT p.tipoPersona, COUNT(p) FROM Persona p GROUP BY p.tipoPersona")
    java.util.List<Object[]> countPersonasByTipo();

    // ENTREGA 3: Consultar conductores ('C') cuya fecha de vigencia de licencia sea anterior a la fecha actual
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Persona p WHERE p.tipoPersona = 'C' AND p.fechaVigenciaLicencia IS NOT NULL AND p.fechaVigenciaLicencia < CURRENT_DATE")
    List<Persona> findConductoresConLicenciaVencida();
}