package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.entities.Persona;
import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoPersona;
import com.PPOOII.proyecto.repository.PersonaRepository;
import com.PPOOII.proyecto.repository.VehiculoPersonaRepository;
import com.PPOOII.proyecto.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class VehiculoPersonaService {

    @Autowired
    private VehiculoPersonaRepository vehiculoPersonaRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Transactional
    public VehiculoPersona asociarConductorAVehiculo(Long personaId, Long vehiculoId, String estadoInicial) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + personaId));

        if (!"C".equalsIgnoreCase(persona.getTipoPersona())) {
            throw new IllegalArgumentException("La persona seleccionada no es de tipo CONDUCTOR");
        }

        int vId = Integer.parseInt(vehiculoId.toString());
        Vehiculo vehiculo = vehiculoRepository.findById(vId)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado con ID: " + vehiculoId));

        VehiculoPersona vp = new VehiculoPersona();
        vp.setPersona(persona);
        vp.setVehiculo(vehiculo);
        vp.setFechaAsociacion(LocalDate.now());
        vp.setEstadoConductor(estadoInicial != null ? estadoInicial : "EA");

        return vehiculoPersonaRepository.save(vp);
    }

    @Transactional
    public VehiculoPersona cambiarEstadoConductor(Long idRelacion, String nuevoEstado) {
        if (!nuevoEstado.matches("^(PO|EA|RO)$")) {
            throw new IllegalArgumentException("Estado inválido. Los estados permitidos son: PO, EA, RO");
        }

        VehiculoPersona vp = vehiculoPersonaRepository.findById(idRelacion)
                .orElseThrow(() -> new RuntimeException("Relación Vehículo-Persona no encontrada con ID: " + idRelacion));

        vp.setEstadoConductor(nuevoEstado);
        return vehiculoPersonaRepository.save(vp);
    }

    public List<VehiculoPersona> listarPorPersona(Long personaId) {
        return vehiculoPersonaRepository.findByPersonaId(personaId);
    }
}