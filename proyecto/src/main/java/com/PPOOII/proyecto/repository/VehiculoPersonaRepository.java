package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.VehiculoPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoPersonaRepository extends JpaRepository<VehiculoPersona, Long> {

    @Query("SELECT vp FROM VehiculoPersona vp WHERE vp.persona.id = :personaId")
    List<VehiculoPersona> findByPersonaId(@Param("personaId") Long personaId);

    @Query("SELECT vp FROM VehiculoPersona vp WHERE vp.vehiculo.id = :vehiculoId")
    List<VehiculoPersona> findByVehiculoId(@Param("vehiculoId") Long vehiculoId);

    List<VehiculoPersona> findByEstadoConductor(String estadoConductor);

    @Query("SELECT DISTINCT vp.persona FROM VehiculoPersona vp WHERE vp.estadoConductor = 'PO'")
    List<com.PPOOII.proyecto.entities.Persona> findConductoresQuePuedenOperar();
}
