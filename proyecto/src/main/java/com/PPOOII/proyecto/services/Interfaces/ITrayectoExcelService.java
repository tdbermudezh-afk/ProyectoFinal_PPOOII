// Paquete de interfaces de servicio según arquitectura de Laboratorio 4
package com.PPOOII.proyecto.services.Interfaces;

// Importaciones de entidades y utilidades de Spring Multipart
import com.PPOOII.proyecto.entities.TrayectoTmp;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

// Interfaz que define las operaciones para el cargue masivo de trayectos vía Excel
public interface ITrayectoExcelService {

    // Procesa el archivo Excel multipart, invoca los 3 procedimientos y retorna el balance del cargue
    Map<String, Object> procesarCargueExcel(MultipartFile archivo, String loginUsuario);

    // Consulta los registros asociados a un identificador numérico de cargue específico
    List<TrayectoTmp> consultarPorIdCargue(Double idCargue);

    // Consulta el resumen estadístico de un cargue (total registros, procesados, errores)
    Map<String, Object> obtenerResumenCargue(Double idCargue);
}

