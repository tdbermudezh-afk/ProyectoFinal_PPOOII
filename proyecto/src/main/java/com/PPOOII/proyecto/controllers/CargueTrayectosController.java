// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

// Importaciones de servicios, entidades y Swagger OpenAPI
import com.PPOOII.proyecto.entities.TrayectoTmp;
import com.PPOOII.proyecto.services.Interfaces.ITrayectoExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

// Controlador REST para el cargue masivo de trayectos vía Excel y consulta por lote (Entrega 4)
@RestController
// Prefijo de ruta protegido para operaciones de cargue masivo
@RequestMapping("/api/cargue-masivo/trayectos")
// Habilitación de CORS
@CrossOrigin(origins = "*")
// Etiqueta OpenAPI para documentación en Swagger UI
@Tag(name = "Cargue Masivo de Trayectos (Entrega 4)", description = "Servicios protegidos para procesar plantillas Excel e invocar los 3 procedimientos de BD")
public class CargueTrayectosController {

    // Inyección de la interfaz de servicio de Excel
    @Autowired
    private ITrayectoExcelService trayectoExcelService;

    // Endpoint 1: Carga masiva de trayectos mediante archivo Excel (Multipart/form-data)
    @Operation(
            summary = "Cargue masivo de trayectos desde Excel",
            description = "Recibe un archivo Excel (.xlsx / .xls), extrae los datos e invoca secuencialmente los procedimientos CARGAR_TRAYECTOS_EXCEL, VALIDAR_TRAYECTOS_CARGUE y PROCESAR_TRAYECTOS_EXCEL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cargue procesado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Archivo no válido o formato incorrecto"),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token)")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> cargarArchivoExcel(
            @Parameter(description = "Archivo Excel con la plantilla de trayectos", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Login del usuario que realiza la operación (opcional)")
            @RequestParam(value = "login", required = false, defaultValue = "cg1001234567") String login) {

        Map<String, Object> resultado = trayectoExcelService.procesarCargueExcel(file, login);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    // Endpoint 2: Consulta del balance y registros cargados por identificador de cargue
    @Operation(
            summary = "Consultar registros por ID de Cargue",
            description = "Retorna el resumen estadístico y el detalle de cada fila cargada en trayecto_tmp para un idCargue específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta recuperada con éxito"),
            @ApiResponse(responseCode = "404", description = "Identificador de cargue no encontrado")
    })
    @GetMapping("/{idCargue}")
    public ResponseEntity<Map<String, Object>> consultarPorIdCargue(
            @Parameter(description = "Identificador numérico del lote de cargue (ej. 202610170130)", required = true)
            @PathVariable Double idCargue) {

        Map<String, Object> resumen = trayectoExcelService.obtenerResumenCargue(idCargue);
        return ResponseEntity.ok(resumen);
    }

    // Endpoint 3: Listado simple de los registros temporales por ID de cargue
    @Operation(
            summary = "Listar registros crudos de trayecto_tmp por ID de Cargue",
            description = "Retorna la lista directa de entidades TrayectoTmp asociadas a un idCargue."
    )
    @GetMapping("/{idCargue}/detalle")
    public ResponseEntity<List<TrayectoTmp>> listarDetalleCrudo(
            @Parameter(description = "Identificador numérico del lote de cargue", required = true)
            @PathVariable Double idCargue) {

        List<TrayectoTmp> detalle = trayectoExcelService.consultarPorIdCargue(idCargue);
        return ResponseEntity.ok(detalle);
    }
}

