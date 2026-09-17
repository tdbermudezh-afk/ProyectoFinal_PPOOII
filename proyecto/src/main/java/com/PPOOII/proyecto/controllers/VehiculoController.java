// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.exceptions.ResourceNotFoundException;
import com.PPOOII.proyecto.services.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador REST que expone los endpoints para la gestión integral y búsqueda de vehículos
@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin(origins = "*")
@Tag(name = "Vehículos", description = "Endpoints para la gestión e inspección de vehículos")
public class VehiculoController {

    // Inyección de la capa de servicio que contiene las reglas de negocio
    private final VehiculoService vehiculoService;

    // Constructor para inyección de dependencias
    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    // Endpoint GET: Retorna el listado completo de todos los vehículos registrados
    @Operation(summary = "Obtener todos los vehículos", description = "Retorna una lista completa con todos los vehículos registrados")
    @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente")
    @GetMapping
    public List<Vehiculo> listarTodos() {
        return vehiculoService.listarTodos();
    }

    // Endpoint GET por ID: Busca y retorna los datos de un vehículo según su identificador único
    @Operation(summary = "Buscar vehículo por ID", description = "Retorna la información del vehículo según su identificador único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Vehiculo> buscarPorId(@PathVariable int id) {
        Vehiculo vehiculo = vehiculoService.buscarPorId(id);
        return ResponseEntity.ok(vehiculo);
    }

    // Endpoint GET por Placa: Búsqueda 1 requerida en el documento (búsqueda por número de placa)
    @Operation(summary = "Buscar vehículo por Placa", description = "Obtiene los detalles del vehículo mediante su número de placa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @GetMapping("/placa/{placa}")
    public ResponseEntity<Vehiculo> buscarPorPlaca(@PathVariable String placa) {
        return vehiculoService.buscarPorPlaca(placa)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con la placa: " + placa));
    }

    // Endpoint POST: Registra un nuevo vehículo verificando documentos obligatorios y formato de placa
    @Operation(summary = "Crear nuevo vehículo", description = "Registra un nuevo vehículo validando que sus atributos cumplan los requisitos estipulados y contenga documentos asociados")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (Error de validación)")
    })
    @PostMapping
    public ResponseEntity<Vehiculo> crear(@Valid @RequestBody Vehiculo vehiculo) {
        Vehiculo nuevo = vehiculoService.guardar(vehiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // Endpoint PUT: Actualiza la información de un vehículo existente por su ID (completa el CRUD)
    @Operation(summary = "Actualizar vehículo", description = "Actualiza la información de un vehículo existente según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vehículo actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Vehiculo> actualizar(@PathVariable int id, @Valid @RequestBody Vehiculo vehiculoDetalles) {
        Vehiculo vehiculoActualizado = vehiculoService.actualizar(id, vehiculoDetalles)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con el ID: " + id));
        return ResponseEntity.ok(vehiculoActualizado);
    }

    // Endpoint DELETE: Elimina del sistema el vehículo con el identificador proporcionado
    @Operation(summary = "Eliminar vehículo", description = "Elimina un registro de vehículo según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Vehículo eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint GET por Tipo: Búsqueda 2 requerida en el documento (búsqueda por Automóvil / Motocicleta)
    @Operation(summary = "Buscar vehículos por tipo de vehículo", description = "Retorna una lista de vehículos según su tipo (Automóvil / Motocicleta)")
    @ApiResponse(responseCode = "200", description = "Lista de vehículos recuperada exitosamente")
    @GetMapping("/tipo/{tipoVehiculo}")
    public ResponseEntity<List<Vehiculo>> buscarPorTipoVehiculo(@PathVariable String tipoVehiculo) {
        return ResponseEntity.ok(vehiculoService.buscarPorTipoVehiculo(tipoVehiculo));
    }

    // Endpoint GET por Documento: Búsqueda 3 requerida en el documento (vehículos con un tipo de documento común)
    @Operation(summary = "Buscar vehículos por tipo de documento en común", description = "Retorna vehículos que tengan en común el documento parametrizado especificado por su ID")
    @ApiResponse(responseCode = "200", description = "Lista de vehículos recuperada exitosamente")
    @GetMapping("/documento/{documentoId}")
    public ResponseEntity<List<Vehiculo>> buscarPorTipoDocumento(@PathVariable int documentoId) {
        return ResponseEntity.ok(vehiculoService.buscarPorTipoDocumento(documentoId));
    }

    // Endpoint GET por Estado: Búsqueda 4 requerida en el documento (vehículos según estado Habilitado/Vencido/En Verificación)
    @Operation(summary = "Buscar vehículos por estado del documento asociado", description = "Retorna vehículos según el estado de sus documentos (Habilitado, Vencido, En Verificación)")
    @ApiResponse(responseCode = "200", description = "Lista de vehículos recuperada exitosamente")
    @GetMapping("/estado-documento/{estado}")
    public ResponseEntity<List<Vehiculo>> buscarPorEstadoDocumento(@PathVariable String estado) {
        return ResponseEntity.ok(vehiculoService.buscarPorEstadoDocumento(estado));
    }

    // Endpoint POST para asociar documento: Servicio requerido para agregar documentos a un vehículo existente
    @Operation(summary = "Agregar documento a un vehículo", description = "Asocia un nuevo documento al historial del vehículo especificado por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documento agregado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del documento inválidos"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @PostMapping("/{id}/documentos")
    public ResponseEntity<Vehiculo> agregarDocumento(
            @PathVariable int id, 
            @Valid @RequestBody VehiculoDocumento documento) {
        
        Vehiculo vehiculoActualizado = vehiculoService.agregarDocumentoAVehiculo(id, documento)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con el ID: " + id));
                
        return ResponseEntity.ok(vehiculoActualizado);
    }
}