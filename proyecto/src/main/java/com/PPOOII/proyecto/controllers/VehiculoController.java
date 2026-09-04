package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.services.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin(origins = "*")
public class VehiculoController {

    @Autowired
    private VehiculoService vehiculoService;

    @GetMapping
    public List<Vehiculo> listarTodos() {
        return vehiculoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehiculo> buscarPorId(@PathVariable int id) {
        return vehiculoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
    public Vehiculo crear(@RequestBody Vehiculo vehiculo) {
        return vehiculoService.guardar(vehiculo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}