package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.VehiculoPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoPersonaRepository extends JpaRepository<VehiculoPersona, Long> {

    List<VehiculoPersona> findByPersonaId(Long personaId);

    List<VehiculoPersona> findByVehiculoId(Long vehiculoId);

    List<VehiculoPersona> findByEstadoConductor(String estadoConductor);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT vp.persona FROM VehiculoPersona vp WHERE vp.estadoConductor = 'PO'")
    java.util.List<com.PPOOII.proyecto.entities.Persona> findConductoresQuePuedenOperar();
}