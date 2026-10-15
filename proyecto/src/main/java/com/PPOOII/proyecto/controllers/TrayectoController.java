// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

// Importaciones de entidades, servicios y Swagger OpenAPI
import com.PPOOII.proyecto.entities.Trayecto;
import com.PPOOII.proyecto.services.Interfaces.ITrayectoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Importaciones de utilidades de colecciones
import java.util.List;
import java.util.Map;

// Controlador REST protegido para la gestión y consulta de trayectos y rutas (Entrega 3)
@RestController
// Prefijo de ruta base protegida
@RequestMapping("/api/trayectos")
// Habilitación de CORS para consumo desde clientes web externos
@CrossOrigin(origins = "*")
// Etiqueta para la documentación interactiva en Swagger
@Tag(name = "Trayectos y Rutas (Entrega 3)", description = "Servicios protegidos para la administración y consulta de trayectos de conductores y vehículos")
public class TrayectoController {

    // Inyección de la interfaz de servicio de trayectos
    @Autowired
    private ITrayectoService trayectoService;

    // Endpoint 0: Registrar un nuevo trayecto (parada)
    @Operation(summary = "Registrar trayecto", description = "Crea un trayecto validando que el vehículo tenga documentos 'Habilitado' y el conductor esté 'PO'")
    @PostMapping
    public ResponseEntity<Trayecto> crearTrayecto(@Valid @RequestBody Trayecto trayecto) {
        Trayecto nuevoTrayecto = trayectoService.crearTrayecto(trayecto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTrayecto);
    }

    // Endpoint 1: Consultar ruta por código de ruta visualizando paradas en orden
    @Operation(summary = "Consultar ruta por código", description = "Retorna los trayectos de una ruta ordenados secuencialmente por parada")
    @GetMapping("/ruta/{codigoRuta}")
    public ResponseEntity<List<Trayecto>> consultarRutaPorCodigo(@PathVariable String codigoRuta) {
        List<Trayecto> trayectos = trayectoService.consultarRutaPorCodigo(codigoRuta);
        return ResponseEntity.ok(trayectos);
    }

    // Endpoint 2: Consultar códigos de ruta agrupados para un conductor por su identificación
    @Operation(summary = "Consultar códigos de ruta por conductor", description = "Visualiza únicamente los códigos de ruta agrupados para un conductor específico")
    @GetMapping("/conductor/{identificacion}")
    public ResponseEntity<List<String>> consultarRutasPorConductor(@PathVariable String identificacion) {
        List<String> codigos = trayectoService.consultarRutasPorConductorIdentificacion(identificacion);
        return ResponseEntity.ok(codigos);
    }

    // Endpoint 3: Consultar códigos de ruta y conductor agrupados para un vehículo por placa
    @Operation(summary = "Consultar rutas y conductor por placa de vehículo", description = "Visualiza el código de ruta y conductor asociado de forma agrupada por placa")
    @GetMapping("/vehiculo/{placa}")
    public ResponseEntity<List<Map<String, Object>>> consultarRutasPorVehiculo(@PathVariable String placa) {
        List<Map<String, Object>> resultados = trayectoService.consultarRutasPorVehiculoPlaca(placa);
        return ResponseEntity.ok(resultados);
    }

    // Endpoint 4: Consultar rutas y trayectos inoperables (vehículo no habilitado o conductor en RO)
    @Operation(summary = "Consultar rutas no operables", description = "Visualiza rutas donde el vehículo no está habilitado o el conductor está restringido para operar (RO)")
    @GetMapping("/no-operables")
    public ResponseEntity<List<Map<String, Object>>> consultarRutasNoOperables() {
        List<Map<String, Object>> noOperables = trayectoService.consultarRutasNoOperables();
        return ResponseEntity.ok(noOperables);
    }

    // Endpoint 5: Listar todos los códigos de ruta registrados para facilitar la selección en el front-end
    @Operation(summary = "Listar todos los códigos de ruta", description = "Retorna el listado de todos los códigos de ruta únicos registrados en el sistema")
    @GetMapping("/rutas")
    public ResponseEntity<List<String>> listarTodasLasRutas() {
        return ResponseEntity.ok(trayectoService.listarTodosLosCodigosRuta());
    }
}

