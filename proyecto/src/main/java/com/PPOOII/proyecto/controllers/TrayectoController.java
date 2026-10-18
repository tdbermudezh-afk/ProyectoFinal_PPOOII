// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

// Importación de la entidad Trayecto
import com.PPOOII.proyecto.entities.Trayecto;
// Importación de la interfaz de servicio ITrayectoService (Laboratorio 1)
import com.PPOOII.proyecto.services.Interfaces.ITrayectoService;
// Importación de @Operation de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.Operation;
// Importación de @Content de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.media.Content;
// Importación de @Schema de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.media.Schema;
// Importación de @ApiResponse de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// Importación de @ApiResponses de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// Importación de @Tag de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.tags.Tag;
// Importación de @Valid para validación de entidades
import jakarta.validation.Valid;
// Importación de @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de HttpStatus para códigos de estado HTTP
import org.springframework.http.HttpStatus;
// Importación de ResponseEntity para respuestas HTTP
import org.springframework.http.ResponseEntity;
// Importación de anotaciones web de Spring MVC
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importación de la interfaz List de Java
import java.util.List;
// Importación de la interfaz Map de Java
import java.util.Map;

// Controlador REST protegido para la gestión y consulta de trayectos y rutas (Entrega 3)
@RestController
@RequestMapping("/api/trayectos")
@CrossOrigin(origins = "*")
@Tag(name = "Trayectos y Rutas (Entrega 3)", description = "Servicios protegidos para la administración y consulta de trayectos de conductores y vehículos")
public class TrayectoController {

    // Inyección de la interfaz de servicio de trayectos
    @Autowired
    private ITrayectoService trayectoService;

    // Endpoint 0: Registrar un nuevo trayecto (parada)
    @Operation(summary = "Registrar trayecto", description = "Crea un trayecto validando que el vehículo tenga documentos 'Habilitado' y el conductor esté 'PO'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trayecto registrado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Trayecto.class))),
            @ApiResponse(responseCode = "400", description = "Vehículo con documentos no habilitados, conductor no autorizado (distinto de PO) o datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Trayecto> crearTrayecto(@Valid @RequestBody Trayecto trayecto) {
        // Invoca el servicio para validar y guardar el nuevo trayecto
        Trayecto nuevoTrayecto = trayectoService.crearTrayecto(trayecto);
        // Retorna HTTP 201 Created con el trayecto creado
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTrayecto);
    }

    // Endpoint 1: Consultar ruta por código de ruta visualizando paradas en orden
    @Operation(summary = "Consultar ruta por código", description = "Retorna los trayectos de una ruta ordenados secuencialmente por parada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paradas de la ruta obtenidas exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Trayecto.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @GetMapping("/ruta/{codigoRuta}")
    public ResponseEntity<List<Trayecto>> consultarRutaPorCodigo(@PathVariable String codigoRuta) {
        // Consulta las paradas ordenadas por el código de ruta
        List<Trayecto> trayectos = trayectoService.consultarRutaPorCodigo(codigoRuta);
        // Retorna HTTP 200 OK con la lista de paradas
        return ResponseEntity.ok(trayectos);
    }

    // Endpoint 2: Consultar códigos de ruta agrupados para un conductor por su identificación
    @Operation(summary = "Consultar códigos de ruta por conductor", description = "Visualiza únicamente los códigos de ruta agrupados para un conductor específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Códigos de ruta del conductor obtenidos exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @GetMapping("/conductor/{identificacion}")
    public ResponseEntity<List<String>> consultarRutasPorConductor(@PathVariable String identificacion) {
        // Consulta los códigos de ruta distintos asociados a la identificación del conductor
        List<String> codigos = trayectoService.consultarRutasPorConductorIdentificacion(identificacion);
        // Retorna HTTP 200 OK con la lista de códigos de ruta
        return ResponseEntity.ok(codigos);
    }

    // Endpoint 3: Consultar códigos de ruta y conductor agrupados para un vehículo por placa
    @Operation(summary = "Consultar rutas y conductor por placa de vehículo", description = "Visualiza el código de ruta y conductor asociado de forma agrupada por placa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rutas del vehículo obtenidas exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @GetMapping("/vehiculo/{placa}")
    public ResponseEntity<List<Map<String, Object>>> consultarRutasPorVehiculo(@PathVariable String placa) {
        // Consulta las rutas y conductores agrupados por la placa del vehículo
        List<Map<String, Object>> resultados = trayectoService.consultarRutasPorVehiculoPlaca(placa);
        // Retorna HTTP 200 OK con el listado agrupado
        return ResponseEntity.ok(resultados);
    }

    // Endpoint 4: Consultar rutas y trayectos inoperables (vehículo no habilitado o conductor en RO)
    @Operation(summary = "Consultar rutas no operables", description = "Visualiza rutas donde el vehículo no está habilitado o el conductor está restringido para operar (RO).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rutas no operables obtenidas exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @GetMapping("/no-operables")
    public ResponseEntity<List<Map<String, Object>>> consultarRutasNoOperables() {
        // Consulta los trayectos que presentan restricciones operativas
        List<Map<String, Object>> noOperables = trayectoService.consultarRutasNoOperables();
        // Retorna HTTP 200 OK con el detalle de las rutas no operables
        return ResponseEntity.ok(noOperables);
    }

    // Endpoint 5: Listar todos los códigos de ruta registrados para facilitar la selección en el front-end
    @Operation(summary = "Listar todos los códigos de ruta", description = "Retorna el listado de todos los códigos de ruta únicos registrados en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de códigos de ruta obtenido exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @GetMapping("/rutas")
    public ResponseEntity<List<String>> listarTodasLasRutas() {
        // Retorna HTTP 200 OK con todos los códigos de ruta distintos
        return ResponseEntity.ok(trayectoService.listarTodosLosCodigosRuta());
    }
}
