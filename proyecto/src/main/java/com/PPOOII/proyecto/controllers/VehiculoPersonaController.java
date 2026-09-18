package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.VehiculoPersona;
import com.PPOOII.proyecto.services.VehiculoPersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/asignaciones")
@CrossOrigin(origins = "*")
@Tag(name = "Asignaciones Conductor-Vehículo", description = "Endpoints para gestionar la relación y estados entre vehículos y conductores")
public class VehiculoPersonaController {

    @Autowired
    private VehiculoPersonaService vehiculoPersonaService;

    // Punto 5: Asociar conductor a vehículo
    @Operation(summary = "Asociar conductor a vehículo", description = "Asigna un vehículo a un conductor específico")
    @PostMapping("/conductor/{personaId}/vehiculo/{vehiculoId}")
    public ResponseEntity<VehiculoPersona> asociarConductor(
            @PathVariable Long personaId,
            @PathVariable Long vehiculoId,
            @RequestBody(required = false) Map<String, String> body) {
        
        String estadoInicial = (body != null) ? body.get("estadoInicial") : "EA";
        VehiculoPersona nuevaAsignacion = vehiculoPersonaService.asociarConductorAVehiculo(personaId, vehiculoId, estadoInicial);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaAsignacion);
    }

    // Punto 6: Cambiar el estado del conductor en relación con el vehículo
    @Operation(summary = "Cambiar estado del conductor", description = "Actualiza el estado de la asignación (PO - Puede Operar, EA - Espera de Aprobación, RO - Restringido para Operar)")
    @PutMapping("/{idRelacion}/estado")
    public ResponseEntity<VehiculoPersona> cambiarEstadoConductor(
            @PathVariable Long idRelacion,
            @RequestBody Map<String, String> body) {
        
        String nuevoEstado = body.get("estado");
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        VehiculoPersona asignacionActualizada = vehiculoPersonaService.cambiarEstadoConductor(idRelacion, nuevoEstado);
        return ResponseEntity.ok(asignacionActualizada);
    }
}