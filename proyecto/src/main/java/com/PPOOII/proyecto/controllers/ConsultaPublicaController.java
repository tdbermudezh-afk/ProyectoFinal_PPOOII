// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

// Importación de la entidad Persona
import com.PPOOII.proyecto.entities.Persona;
// Importación de la entidad Trayecto
import com.PPOOII.proyecto.entities.Trayecto;
// Importación de la entidad Vehiculo
import com.PPOOII.proyecto.entities.Vehiculo;
// Importación de las interfaces de servicio (Separación estricta de capas @RestController -> @Service -> @Repository)
import com.PPOOII.proyecto.services.Interfaces.IPersonaService;
import com.PPOOII.proyecto.services.Interfaces.ITrayectoService;
import com.PPOOII.proyecto.services.Interfaces.IVehiculoDocumentoService;
import com.PPOOII.proyecto.services.Interfaces.IVehiculoPersonaService;
import com.PPOOII.proyecto.services.Interfaces.IVehiculoService;
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
// Importación de @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de @Value para inyectar propiedades desde application.properties
import org.springframework.beans.factory.annotation.Value;
// Importación de ResponseEntity para respuestas HTTP
import org.springframework.http.ResponseEntity;
// Importación de anotaciones web de Spring MVC
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Importación de la interfaz List de Java
import java.util.List;
// Importación de la interfaz Map de Java
import java.util.Map;
// Importación del contenedor Optional de Java
import java.util.Optional;

// Controlador REST de consultas públicas que no requieren autenticación (APIKey ni Token JWT)
@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
@Tag(name = "Consultas Públicas", description = "Servicios de consulta pública (Entrega 2 y Front-End de Entrega 3) que no requieren autenticación (x-api-key ni token)")
public class ConsultaPublicaController {

    // Inyección del servicio de documentos asociados a vehículos
    @Autowired
    private IVehiculoDocumentoService vehiculoDocumentoService;

    // Inyección del servicio de relación vehículo-conductor
    @Autowired
    private IVehiculoPersonaService vehiculoPersonaService;

    // Inyección del servicio de vehículos
    @Autowired
    private IVehiculoService vehiculoService;

    // Inyección del servicio de personas
    @Autowired
    private IPersonaService personaService;

    // Inyección del servicio de trayectos para visualización pública de rutas en el mapa
    @Autowired
    private ITrayectoService trayectoService;

    // Inyección de la clave de Google Maps configurada en application.properties
    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    // 1. Consultar vehículos con documentos vencidos
    @Operation(summary = "Vehículos con documentos vencidos", description = "Retorna la lista de vehículos que tienen al menos un documento en estado 'Vencido' o con fecha expirada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de vehículos con documentos vencidos obtenido con éxito", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Vehiculo.class)))
    })
    @GetMapping("/vehiculos/documentos-vencidos")
    public ResponseEntity<List<Vehiculo>> obtenerVehiculosDocVencidos() {
        // Retorna HTTP 200 OK con los vehículos encontrados mediante la capa de servicio
        return ResponseEntity.ok(vehiculoDocumentoService.obtenerVehiculosConDocumentosVencidos());
    }

    // 2. Consultar todos los conductores que puedan operar
    @Operation(summary = "Conductores autorizados para operar (PO)", description = "Retorna las personas conductoras cuyo estado operativo es 'PO' (Puede Operar).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de conductores autorizados obtenido con éxito", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Persona.class)))
    })
    @GetMapping("/conductores/autorizados")
    public ResponseEntity<List<Persona>> obtenerConductoresAutorizados() {
        // Retorna HTTP 200 OK con la lista de conductores habilitados mediante la capa de servicio
        return ResponseEntity.ok(vehiculoPersonaService.obtenerConductoresQuePuedenOperar());
    }

    // 3. Consultar vehículo por placa relacionando conductores y documentos
    @Operation(summary = "Detalle completo de un vehículo por su placa", description = "Consulta un vehículo por su placa incluyendo sus documentos asociados y conductores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Vehiculo.class))),
            @ApiResponse(responseCode = "404", description = "No existe un vehículo con la placa especificada", content = @Content)
    })
    @GetMapping("/vehiculos/placa/{placa}/detalles")
    public ResponseEntity<Vehiculo> obtenerDetallesVehiculoPorPlaca(@PathVariable String placa) {
        // Busca el vehículo por su número de placa a través de la capa de servicio
        Optional<Vehiculo> vehiculo = vehiculoService.buscarPorPlaca(placa);
        // Retorna 200 OK si existe o 404 Not Found en caso contrario
        return vehiculo.map(ResponseEntity::ok)
                // Si no se encuentra la placa, construye respuesta 404
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Consultar vehículos con documentos por vencer (param: diasFaltantes)
    @Operation(summary = "Vehículos con documentos por vencer en un lapso de tiempo", description = "Retorna los vehículos cuyos documentos vencen dentro de los próximos N días.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de vehículos con documentos próximos a vencer", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Vehiculo.class)))
    })
    @GetMapping("/vehiculos/documentos-por-vencer")
    public ResponseEntity<List<Vehiculo>> obtenerVehiculosDocPorVencer(
            @RequestParam(defaultValue = "30") int diasFaltantes) {
        // Retorna los vehículos cuyos documentos vencen entre hoy y la fecha límite mediante la capa de servicio
        return ResponseEntity.ok(vehiculoDocumentoService.obtenerVehiculosConDocumentosPorVencer(diasFaltantes));
    }

    // 5. Consultar el total de personas agrupadas por tipo
    @Operation(summary = "Estadística de personas por tipo", description = "Retorna el conteo total de personas agrupadas por tipo ('C' Conductores y 'A' Administrativos).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mapa estadístico obtenido con éxito", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/personas/agrupadas-por-tipo")
    public ResponseEntity<Map<String, Long>> obtenerTotalPersonasPorTipo() {
        // Retorna HTTP 200 OK con el mapa estadístico calculado por el servicio de personas
        return ResponseEntity.ok(personaService.obtenerTotalPersonasPorTipo());
    }

    // 6. Obtener clave de Google Maps configurada para el Front-End (Entrega 3)
    @Operation(summary = "Obtener configuración de Google Maps para el Front-End", description = "Retorna la API Key de Google Maps configurada en el servidor para inicializar el visor cartográfico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuración obtenida con éxito", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/config/maps-key")
    public ResponseEntity<Map<String, String>> obtenerConfigGoogleMaps() {
        // Retorna el mapa con la clave de Google Maps o cadena vacía si se usa el modo híbrido Leaflet/OSRM
        return ResponseEntity.ok(Map.of("apiKey", googleMapsApiKey != null ? googleMapsApiKey : ""));
    }

    // 7. Listar todas las rutas registradas para el selector del Front-End (Entrega 3)
    @Operation(summary = "Listar todas las rutas registradas", description = "Retorna los códigos únicos de ruta disponibles para su visualización en el mapa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de códigos de ruta obtenida con éxito", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/rutas")
    public ResponseEntity<List<String>> listarRutasPublicas() {
        // Retorna HTTP 200 OK con los códigos de ruta distintos
        return ResponseEntity.ok(trayectoService.listarTodosLosCodigosRuta());
    }

    // 8. Obtener paradas y detalle de una ruta para visualización en el mapa (Entrega 3)
    @Operation(summary = "Obtener paradas y detalle de una ruta para visualización en el mapa", description = "Retorna las paradas ordenadas secuencialmente junto con la información del vehículo y conductor asociados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paradas de la ruta obtenidas con éxito", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Trayecto.class)))
    })
    @GetMapping("/rutas/{codigoRuta}/paradas")
    public ResponseEntity<List<Trayecto>> obtenerParadasRutaPublica(@PathVariable String codigoRuta) {
        // Retorna HTTP 200 OK con la lista de paradas ordenadas por ordenParada ascendente
        return ResponseEntity.ok(trayectoService.consultarRutaPorCodigo(codigoRuta));
    }
}
