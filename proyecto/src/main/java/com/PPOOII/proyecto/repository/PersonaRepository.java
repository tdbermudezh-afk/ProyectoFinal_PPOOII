package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    Optional<Persona> findByIdentificacion(String identificacion);

    List<Persona> findByTipoPersona(String tipoPersona);

    boolean existsByIdentificacion(String identificacion);

    boolean existsByCorreo(String correo);

    @Query("SELECT p.tipoPersona, COUNT(p) FROM Persona p GROUP BY p.tipoPersona")
    List<Object[]> countPersonasByTipo();

    // ENTREGA 3: Consultar conductores ('C') cuya fecha de vigencia de licencia sea anterior a la fecha actual
    @Query("SELECT p FROM Persona p WHERE p.tipoPersona = 'C' AND p.fechaVigenciaLicencia IS NOT NULL AND p.fechaVigenciaLicencia < :hoy")
    List<Persona> findConductoresConLicenciaVencida(@Param("hoy") LocalDate hoy);

    default List<Persona> findConductoresConLicenciaVencida() {
        return findConductoresConLicenciaVencida(LocalDate.now());
    }
}
