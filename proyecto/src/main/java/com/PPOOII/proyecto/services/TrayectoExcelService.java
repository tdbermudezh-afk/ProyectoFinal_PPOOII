// Paquete de servicios de la aplicación
package com.PPOOII.proyecto.services;

// Importaciones de entidades, repositorios y contratos de interfaz
import com.PPOOII.proyecto.entities.TrayectoTmp;
import com.PPOOII.proyecto.repository.TrayectoTmpRepository;
import com.PPOOII.proyecto.services.Interfaces.ITrayectoExcelService;

// Importaciones de Apache POI para lectura y escritura de archivos Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// Importaciones de utilidades Java
import java.io.ByteArrayOutputStream;
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
        Long idCargue = Long.parseLong(formattedDateTime);

        int totalFilasLeidas = 0;
        String usuarioEfectivo = (loginUsuario != null && !loginUsuario.isBlank()) ? loginUsuario.trim() : "cg1001234567";

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
                // Si la plantilla incluye columna 7 con login específico por fila, respetarla; de lo contrario usar usuarioEfectivo
                String loginFila = obtenerValorCeldaComoTexto(row.getCell(7));
                String loginARegistrar = (loginFila != null && !loginFila.isBlank()) ? loginFila : usuarioEfectivo;

                // Si toda la fila está vacía, ignorarla
                if (esFilaVacia(idPersona, idVehiculo, codigoRuta, ubicacion)) {
                    continue;
                }

                // Invocación del Método 1 (Procedimiento CARGAR_TRAYECTOS_EXCEL) según Laboratorio 4 Complementario
                if (this.cargarDatosExcel(idPersona, idVehiculo, codigoRuta, ubicacion,
                        ordenParada, latitud, longitud, loginARegistrar, idCargue)) { // Verifica si el cargue de la fila fue exitoso
                    totalFilasLeidas++; // Incrementa el contador de filas cargadas
                } // Fin de invocación de cargarDatosExcel
            } // Fin del ciclo for sobre las filas del Excel

            logger.info("[FASE 1 COMPLETADA] Filas cargadas en trayecto_tmp: {}", totalFilasLeidas); // Log de fase 1

            // FASE 2 y FASE 3 encadenadas mediante métodos booleanos según Laboratorio 4 Complementario (pág. 9)
            if (this.validarDatosExcel(idCargue)) { // Ejecuta y verifica el procedimiento de validación de reglas de negocio
                logger.info("Validación de datos cargados. EXITOSO!!!!!"); // Mensaje de éxito de validación
                if (this.procesarDatosExcel(idCargue)) { // Ejecuta y verifica el procedimiento de procesado a tabla transaccional
                    logger.info("Procesado de datos validados. EXITOSO!!!!!"); // Mensaje de éxito de procesado
                } // Fin de procesarDatosExcel
            } // Fin de validarDatosExcel

        } catch (Exception e) { // Captura cualquier excepción en la lectura del archivo Excel
            logger.error("[ERROR EN PROCESAMIENTO EXCEL] idCargue: {}, Causa: {}", idCargue, e.getMessage()); // Registra el error
            throw new RuntimeException("Error al procesar el archivo Excel: " + e.getMessage(), e); // Propaga la excepción
        } // Fin del bloque try-catch

        // Construir balance final del cargue masivo
        return obtenerResumenCargue(idCargue); // Retorna el mapa con las estadísticas y registros del cargue
    } // Fin del método procesarCargueExcel

    // Implementación del método 1 del Laboratorio 4 Complementario: Invoca el procedimiento CARGAR_TRAYECTOS_EXCEL
    @Override // Sobrescribe el método definido en ITrayectoExcelService
    public boolean cargarDatosExcel(String idPersona, String idVehiculo, String codigoRuta, String ubicacion,
                                    String ordenParada, String latitud, String longitud, String loginUsuario, Long idCargue) { // Parámetros de la fila Excel
        try { // Bloque try para controlar la invocación del procedimiento almacenado
            trayectoTmpRepository.CARGAR_TRAYECTOS_EXCEL(
                    idPersona, idVehiculo, codigoRuta, ubicacion,
                    ordenParada, latitud, longitud, loginUsuario, idCargue
            ); // Llama al procedimiento almacenado en MySQL
            logger.info("Se carga registro de excel a tabla temporal de forma exitosa!!!!"); // Log de confirmación
            return true; // Retorna verdadero indicando éxito
        } catch (Exception e) { // Captura errores en la invocación del procedimiento
            logger.error("ERROR CARGAR DATOS DE ARCHIVO EXCEL: PROBLEMAS CON LA INVOCACIÓN DEL PROCEDIMIENTO ALMACENADO: {}", e.getMessage()); // Log de error
            return false; // Retorna falso en caso de fallo
        } // Fin del bloque try-catch
    } // Fin del método cargarDatosExcel

    // Implementación del método 2 del Laboratorio 4 Complementario: Invoca el procedimiento VALIDAR_TRAYECTOS_CARGUE
    @Override // Sobrescribe el método definido en ITrayectoExcelService
    public boolean validarDatosExcel(Long idCargue) { // Recibe el identificador único del lote de cargue
        try { // Bloque try para controlar la invocación del procedimiento de validación
            trayectoTmpRepository.VALIDAR_TRAYECTOS_CARGUE(idCargue); // Llama al procedimiento almacenado VALIDAR_TRAYECTOS_CARGUE
            logger.info("Se valida registro de excel en tabla temporal de forma exitosa!!!!"); // Log de confirmación
            return true; // Retorna verdadero indicando éxito
        } catch (Exception e) { // Captura errores durante la validación en base de datos
            logger.error("ERROR VALIDANDO DATOS DE ARCHIVO EXCEL: PROBLEMAS CON LA INVOCACIÓN DEL PROCEDIMIENTO ALMACENADO: {}", e.getMessage()); // Log de error
            return false; // Retorna falso en caso de fallo
        } // Fin del bloque try-catch
    } // Fin del método validarDatosExcel

    // Implementación del método 3 del Laboratorio 4 Complementario: Invoca el procedimiento PROCESAR_TRAYECTOS_EXCEL
    @Override // Sobrescribe el método definido en ITrayectoExcelService
    public boolean procesarDatosExcel(Long idCargue) { // Recibe el identificador único del lote de cargue
        try { // Bloque try para controlar la invocación del procedimiento de procesado
            trayectoTmpRepository.PROCESAR_TRAYECTOS_EXCEL(idCargue); // Llama al procedimiento almacenado PROCESAR_TRAYECTOS_EXCEL
            logger.info("Se procesan los datos validados de forma exitosa y se trasladan a la tabla transaccional!!!"); // Log de confirmación
            return true; // Retorna verdadero indicando éxito
        } catch (Exception e) { // Captura errores durante el traslado a la tabla transaccional
            logger.error("ERROR PROCESAR DATOS DE ARCHIVO EXCEL: PROBLEMAS CON LA INVOCACIÓN DEL PROCEDIMIENTO ALMACENADO: {}", e.getMessage()); // Log de error
            return false; // Retorna falso en caso de fallo
        } // Fin del bloque try-catch
    } // Fin del método procesarDatosExcel

    // Consulta de los registros de un cargue específico
    @Override
    @Transactional(readOnly = true)
    public List<TrayectoTmp> consultarPorIdCargue(Long idCargue) {
        return trayectoTmpRepository.findByIdCargueOrderByIdAsc(idCargue);
    }

    // Consulta consolidada del resultado de un cargue
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerResumenCargue(Long idCargue) {
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

    // Genera en memoria una plantilla Excel (.xlsx) de ejemplo con casos válidos e inválidos
    @Override
    public byte[] generarPlantillaExcelEjemplo() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Trayectos_Ibague");
            Row header = sheet.createRow(0);
            String[] columnas = {"ID_PERSONA_O_CEDULA", "ID_VEHICULO_O_PLACA", "CODIGO_RUTA", "UBICACION", "ORDEN_PARADA", "LATITUD", "LONGITUD"};
            for (int i = 0; i < columnas.length; i++) {
                header.createCell(i).setCellValue(columnas[i]);
            }

            String[][] filas = {
                {"1010203040", "ABC123", "RUTA-CENTRO-IBG", "Plaza de Bolívar, Ibagué, Tolima", "0", "4.4447", "-75.2424"},
                {"1010203040", "ABC123", "RUTA-CENTRO-IBG", "Conservatorio del Tolima, Ibagué, Tolima", "1", "4.4439", "-75.2412"},
                {"1010203040", "ABC123", "RUTA-CENTRO-IBG", "Parque Murillo Toro, Ibagué, Tolima", "2", "4.4432", "-75.2398"},
                {"1020304050", "XYZ789", "RUTA-ERR-DOC", "Terminal de Transportes de Ibagué, Tolima", "0", "4.4395", "-75.2320"},
                {"1001234567", "ABC123", "RUTA-ERR-ADMIN", "Universidad del Tolima, Ibagué, Tolima", "0", "4.4275", "-75.2145"}
            };

            for (int r = 0; r < filas.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < filas[r].length; c++) {
                    row.createCell(c).setCellValue(filas[r][c]);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No fue posible generar la plantilla Excel: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarLotesCargue() {
        return trayectoTmpRepository.findDistinctIdCargueDesc();
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
