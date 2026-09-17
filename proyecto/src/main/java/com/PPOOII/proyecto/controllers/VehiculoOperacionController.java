package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.entities.VehiculoPersona;
import com.PPOOII.proyecto.services.VehiculoDocumentoService;
import com.PPOOII.proyecto.services.VehiculoPersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operaciones")
public class VehiculoOperacionController {

    @Autowired
    private VehiculoDocumentoService vehiculoDocumentoService;

    @Autowired
    private VehiculoPersonaService vehiculoPersonaService;

    // 1. Carga masiva/individual de documentos PDF Base64
    @PostMapping("/documentos/cargar")
    public ResponseEntity<List<VehiculoDocumento>> cargarDocumentos(@RequestBody List<Map<String, Object>> documentosPayload) {
        List<VehiculoDocumento> resultado = vehiculoDocumentoService.cargarDocumentos(documentosPayload);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    // 2. Asociar vehículos a un conductor específico
    @PostMapping("/conductores/asociar")
    public ResponseEntity<VehiculoPersona> asociarConductor(
            @RequestParam Long personaId,
            @RequestParam Long vehiculoId,
            @RequestParam(required = false, defaultValue = "EA") String estadoConductor) {
        
        VehiculoPersona vp = vehiculoPersonaService.asociarConductorAVehiculo(personaId, vehiculoId, estadoConductor);
        return ResponseEntity.status(HttpStatus.CREATED).body(vp);
    }

    // 3. Cambiar estado del conductor (PO, EA, RO)
    @PutMapping("/conductores/relacion/{idRelacion}/estado")
    public ResponseEntity<VehiculoPersona> cambiarEstadoConductor(
            @PathVariable Long idRelacion,
            @RequestBody Map<String, String> payload) {
        
        String nuevoEstado = payload.get("estadoConductor");
        VehiculoPersona vpActualizado = vehiculoPersonaService.cambiarEstadoConductor(idRelacion, nuevoEstado);
        return ResponseEntity.ok(vpActualizado);
    }
}