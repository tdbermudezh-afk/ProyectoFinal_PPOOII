package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.exceptions.ResourceNotFoundException;
import com.PPOOII.proyecto.services.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin(origins = "*")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public List<Vehiculo> listarTodos() {
        return vehiculoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehiculo> buscarPorId(@PathVariable int id) {
        Vehiculo vehiculo = vehiculoService.buscarPorId(id);
        return ResponseEntity.ok(vehiculo);
    }

    @GetMapping("/placa/{placa}")
    public ResponseEntity<Vehiculo> buscarPorPlaca(@PathVariable String placa) {
        return vehiculoService.buscarPorPlaca(placa)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tipo/{tipoVehiculo}")
    public List<Vehiculo> buscarPorTipo(@PathVariable String tipoVehiculo) {
        return vehiculoService.buscarPorTipoVehiculo(tipoVehiculo);
    }

    @GetMapping("/documento/{documentoId}")
    public List<Vehiculo> buscarPorDocumento(@PathVariable int documentoId) {
        return vehiculoService.buscarPorTipoDocumento(documentoId);
    }

    @GetMapping("/estado/{estado}")
    public List<Vehiculo> buscarPorEstado(@PathVariable String estado) {
        return vehiculoService.buscarPorEstadoDocumento(estado);
    }

    @PostMapping
    public Vehiculo crear(@Valid @RequestBody Vehiculo vehiculo) {
        return vehiculoService.guardar(vehiculo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/documentos")
    public ResponseEntity<Vehiculo> agregarDocumento(
        @PathVariable int id, 
        @RequestBody VehiculoDocumento documento) {
    
    Vehiculo vehiculoActualizado = vehiculoService.agregarDocumentoAVehiculo(id, documento)
            .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado con id: " + id));
            
    return ResponseEntity.ok(vehiculoActualizado);
    }
}
