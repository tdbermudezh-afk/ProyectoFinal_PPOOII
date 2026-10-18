// Paquete de interfaces de servicio según arquitectura de Laboratorio 4
package com.PPOOII.proyecto.services.Interfaces;

// Importaciones de entidades y utilidades de Spring Multipart
import com.PPOOII.proyecto.entities.TrayectoTmp;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

// Interfaz que define las operaciones para el cargue masivo de trayectos vía Excel (Laboratorio 4 Complementario)
public interface ITrayectoExcelService { // Declaración de la interfaz ITrayectoExcelService

    // Procesa el archivo Excel multipart, invoca los 3 procedimientos y retorna el balance del cargue
    Map<String, Object> procesarCargueExcel(MultipartFile archivo, String loginUsuario); // Método orquestador del cargue

    // Método 1 del Laboratorio 4 Complementario: Invoca el procedimiento almacenado CARGAR_TRAYECTOS_EXCEL
    boolean cargarDatosExcel(String idPersona, String idVehiculo, String codigoRuta, String ubicacion,
                             String ordenParada, String latitud, String longitud, String loginUsuario, Double idCargue); // Retorna true si se insertó en trayecto_tmp

    // Método 2 del Laboratorio 4 Complementario: Invoca el procedimiento almacenado VALIDAR_TRAYECTOS_CARGUE
    boolean validarDatosExcel(Double idCargue); // Retorna true si la validación se ejecutó exitosamente

    // Método 3 del Laboratorio 4 Complementario: Invoca el procedimiento almacenado PROCESAR_TRAYECTOS_EXCEL
    boolean procesarDatosExcel(Double idCargue); // Retorna true si el procesado hacia la tabla trayecto fue exitoso

    // Consulta los registros asociados a un identificador numérico de cargue específico
    List<TrayectoTmp> consultarPorIdCargue(Double idCargue); // Retorna la lista de registros temporales por idCargue

    // Consulta el resumen estadístico de un cargue (total registros, procesados, errores)
    Map<String, Object> obtenerResumenCargue(Double idCargue); // Retorna el balance consolidado del cargue
} // Fin de la interfaz ITrayectoExcelService


