// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

// Importación de la entidad temporal TrayectoTmp (Laboratorio 4 Complementario)
import com.PPOOII.proyecto.entities.TrayectoTmp;
// Importación de la interfaz de servicio ITrayectoExcelService (Laboratorio 4 Complementario)
import com.PPOOII.proyecto.services.Interfaces.ITrayectoExcelService;
// Importación de @Operation de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.Operation;
// Importación de @Parameter de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.Parameter;
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
// Importación de HttpStatus para códigos de estado HTTP
import org.springframework.http.HttpStatus;
// Importación de MediaType para soporte de multipart/form-data
import org.springframework.http.MediaType;
// Importación de ResponseEntity para respuestas HTTP
import org.springframework.http.ResponseEntity;
// Importación de anotaciones web de Spring MVC
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// Importación de MultipartFile para recepción de archivos Excel (Laboratorio 4 Complementario)
import org.springframework.web.multipart.MultipartFile;

// Importación de la interfaz List de Java
import java.util.List;
// Importación de la interfaz Map de Java
import java.util.Map;

// Controlador REST para el cargue masivo de trayectos vía Excel y consulta por lote (Entrega 4 / Laboratorio 4 Complementario)
@RestController
@RequestMapping("/api/cargue-masivo/trayectos")
@CrossOrigin(origins = "*")
@Tag(name = "Cargue Masivo de Trayectos (Entrega 4)", description = "Servicios protegidos para procesar plantillas Excel Apache POI e invocar los 3 procedimientos almacenados de BD")
public class CargueTrayectosController {

    // Inyección de la interfaz de servicio de Excel
    @Autowired
    private ITrayectoExcelService trayectoExcelService;

    // Endpoint 1: Carga masiva de trayectos mediante archivo Excel (Multipart/form-data)
    @Operation(
            summary = "Cargue masivo de trayectos desde Excel",
            description = "Recibe un archivo Excel (.xlsx / .xls), extrae los datos con Apache POI e invoca secuencialmente los métodos cargarDatosExcel (CARGAR_TRAYECTOS_EXCEL), validarDatosExcel (VALIDAR_TRAYECTOS_CARGUE) y procesarDatosExcel (PROCESAR_TRAYECTOS_EXCEL)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cargue procesado exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Archivo no válido o formato incorrecto", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> cargarArchivoExcel(
            @Parameter(description = "Archivo Excel con la plantilla de trayectos", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Login del usuario que realiza la operación (opcional)")
            @RequestParam(value = "login", required = false, defaultValue = "cg1001234567") String login) {

        // Ejecuta el flujo completo de cargue, validación y procesamiento mediante el servicio
        Map<String, Object> resultado = trayectoExcelService.procesarCargueExcel(file, login);
        // Retorna HTTP 201 Created con el resumen y detalle del lote procesado
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    // Endpoint 2: Consulta del balance y registros cargados por identificador de cargue
    @Operation(
            summary = "Consultar registros por ID de Cargue",
            description = "Retorna el resumen estadístico y el detalle de cada fila cargada en trayecto_tmp para un idCargue específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta recuperada con éxito", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Identificador de cargue no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @GetMapping("/{idCargue}")
    public ResponseEntity<Map<String, Object>> consultarPorIdCargue(
            @Parameter(description = "Identificador numérico del lote de cargue (ej. 202610170130)", required = true)
            @PathVariable Double idCargue) {

        // Obtiene el balance estadístico y los registros asociados al idCargue
        Map<String, Object> resumen = trayectoExcelService.obtenerResumenCargue(idCargue);
        // Retorna HTTP 200 OK con el mapa de resumen
        return ResponseEntity.ok(resumen);
    }

    // Endpoint 3: Listado simple de los registros temporales por ID de cargue
    @Operation(
            summary = "Listar registros crudos de trayecto_tmp por ID de Cargue",
            description = "Retorna la lista directa de entidades TrayectoTmp asociadas a un idCargue."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de registros temporales obtenido con éxito", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrayectoTmp.class))),
            @ApiResponse(responseCode = "404", description = "Identificador de cargue no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @GetMapping("/{idCargue}/detalle")
    public ResponseEntity<List<TrayectoTmp>> listarDetalleCrudo(
            @Parameter(description = "Identificador numérico del lote de cargue", required = true)
            @PathVariable Double idCargue) {

        // Consulta las entidades TrayectoTmp filtradas por idCargue
        List<TrayectoTmp> detalle = trayectoExcelService.consultarPorIdCargue(idCargue);
        // Retorna HTTP 200 OK con la lista de registros temporales
        return ResponseEntity.ok(detalle);
    }
}
