// Paquete de servicios de la aplicación
package com.PPOOII.proyecto.services;

// Importaciones de entidades, repositorios y contratos de interfaz
import com.PPOOII.proyecto.entities.TrayectoTmp;
import com.PPOOII.proyecto.repository.TrayectoTmpRepository;
import com.PPOOII.proyecto.services.Interfaces.ITrayectoExcelService;

// Importaciones de Apache POI para lectura de archivos Excel
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// Importaciones de utilidades Java
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// Servicio Spring que orquesta la lectura del archivo Excel y la ejecución de los 3 procedimientos
@Service("trayectoExcelService")
public class TrayectoExcelService implements ITrayectoExcelService {

    // Logger para trazabilidad de eventos de cargue masivo
    private static final Logger logger = LoggerFactory.getLogger(TrayectoExcelService.class);

    // Inyección del repositorio de la entidad temporal
    @Autowired
    private TrayectoTmpRepository trayectoTmpRepository;

    // Procesamiento integral del archivo Excel aplicando el flujo de 3 fases: CARGADO -> VALIDADO -> PROCESADO
    @Override
    @Transactional
    public Map<String, Object> procesarCargueExcel(MultipartFile archivo, String loginUsuario) {
        // Validar que se haya subido un archivo válido
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Debe suministrar un archivo de Excel no vacío para procesar el cargue.");
        }

        // Generar identificador de cargue numérico basado en año, mes, día, hora y minutos (ej. 202610170130)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String formattedDateTime = LocalDateTime.now().format(formatter);
        Double idCargue = Double.parseDouble(formattedDateTime);

        int totalFilasLeidas = 0;
        String usuarioEfectivo = (loginUsuario != null && !loginUsuario.isBlank()) ? loginUsuario : "SISTEMA";

        logger.info("[CARGUE MASIVO INICIADO] idCargue: {}, Archivo: {}, Usuario: {}", idCargue, archivo.getOriginalFilename(), usuarioEfectivo);

        try (InputStream inputStream = archivo.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            // Tomar la primera hoja de trabajo del libro de Excel
            Sheet sheet = workbook.getSheetAt(0);

            // FASE 1: Iteración sobre las filas e inserción en trayecto_tmp vía procedimiento CARGAR_TRAYECTOS_EXCEL
            for (Row row : sheet) {
                // Omitir fila 0 si corresponde a los encabezados
                if (row.getRowNum() == 0) {
                    continue;
                }

                // Extraer los valores de cada celda de la plantilla
                String idPersona = obtenerValorCeldaComoTexto(row.getCell(0));
                String idVehiculo = obtenerValorCeldaComoTexto(row.getCell(1));
                String codigoRuta = obtenerValorCeldaComoTexto(row.getCell(2));
                String ubicacion = obtenerValorCeldaComoTexto(row.getCell(3));
                String ordenParada = obtenerValorCeldaComoTexto(row.getCell(4));
                String latitud = obtenerValorCeldaComoTexto(row.getCell(5));
                String longitud = obtenerValorCeldaComoTexto(row.getCell(6));

                // Si toda la fila está vacía, ignorarla
                if (esFilaVacia(idPersona, idVehiculo, codigoRuta, ubicacion)) {
                    continue;
                }

                // Invocación del Procedimiento 1: CARGAR_TRAYECTOS_EXCEL
                trayectoTmpRepository.CARGAR_TRAYECTOS_EXCEL(
                        idPersona, idVehiculo, codigoRuta, ubicacion,
                        ordenParada, latitud, longitud, usuarioEfectivo, idCargue
                );
                totalFilasLeidas++;
            }

            logger.info("[FASE 1 COMPLETADA] Filas cargadas en trayecto_tmp: {}", totalFilasLeidas);

            // FASE 2: Invocación del Procedimiento 2: VALIDAR_TRAYECTOS_CARGUE
            logger.info("[FASE 2 INICIADA] Ejecutando procedimiento VALIDAR_TRAYECTOS_CARGUE para idCargue: {}", idCargue);
            trayectoTmpRepository.VALIDAR_TRAYECTOS_CARGUE(idCargue);

            // FASE 3: Invocación del Procedimiento 3: PROCESAR_TRAYECTOS_EXCEL
            logger.info("[FASE 3 INICIADA] Ejecutando procedimiento PROCESAR_TRAYECTOS_EXCEL para idCargue: {}", idCargue);
            trayectoTmpRepository.PROCESAR_TRAYECTOS_EXCEL(idCargue);

        } catch (Exception e) {
            logger.error("[ERROR EN PROCESAMIENTO EXCEL] idCargue: {}, Causa: {}", idCargue, e.getMessage());
            throw new RuntimeException("Error al procesar el archivo Excel: " + e.getMessage(), e);
        }

        // Construir balance final del cargue masivo
        return obtenerResumenCargue(idCargue);
    }

    // Consulta de los registros de un cargue específico
    @Override
    @Transactional(readOnly = true)
    public List<TrayectoTmp> consultarPorIdCargue(Double idCargue) {
        return trayectoTmpRepository.findByIdCargueOrderByIdAsc(idCargue);
    }

    // Consulta consolidada del resultado de un cargue
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerResumenCargue(Double idCargue) {
        List<TrayectoTmp> registros = trayectoTmpRepository.findByIdCargueOrderByIdAsc(idCargue);

        long procesados = registros.stream().filter(r -> "PROCESADO".equalsIgnoreCase(r.getEstado())).count();
        long errores = registros.stream().filter(r -> "ERROR".equalsIgnoreCase(r.getEstado())).count();
        long validados = registros.stream().filter(r -> "VALIDADO".equalsIgnoreCase(r.getEstado())).count();
        long cargados = registros.stream().filter(r -> "CARGADO".equalsIgnoreCase(r.getEstado())).count();

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("idCargue", idCargue);
        resumen.put("totalRegistros", registros.size());
        resumen.put("exitososProcesados", procesados);
        resumen.put("rechazadosConError", errores);
        resumen.put("pendientesValidacion", validados + cargados);
        resumen.put("mensaje", String.format("Cargue finalizado. Total: %d, Exitosos: %d, Errores: %d", 
                registros.size(), procesados, errores));
        resumen.put("detalleRegistros", registros);

        return resumen;
    }

    // Utilidad interna: Convierte el valor de cualquier celda de Excel a cadena de texto limpia
    private String obtenerValorCeldaComoTexto(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    double numVal = cell.getNumericCellValue();
                    // Si es entero, formatear sin decimales
                    if (numVal == Math.floor(numVal)) {
                        return String.valueOf((long) numVal);
                    } else {
                        return String.valueOf(numVal);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
            default:
                return null;
        }
    }

    // Comprueba si una fila no tiene información útil
    private boolean esFilaVacia(String... campos) {
        for (String c : campos) {
            if (c != null && !c.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}

