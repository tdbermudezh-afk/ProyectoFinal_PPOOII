// Paquete de interfaces de servicio según arquitectura de Laboratorio 3
package com.PPOOII.proyecto.services.Interfaces;

// Importaciones de entidades y estructuras de datos
import com.PPOOII.proyecto.entities.Trayecto;
import java.util.List;
import java.util.Map;

// Interfaz que define las operaciones de negocio para la gestión de Trayectos
public interface ITrayectoService {

    // Registra un nuevo trayecto validando que el vehículo tenga documentos 'Habilitado' y conductor en 'PO'
    Trayecto crearTrayecto(Trayecto trayecto);

    // Consulta los trayectos de una ruta específica ordenados secuencialmente
    List<Trayecto> consultarRutaPorCodigo(String codigoRuta);

    // Consulta los códigos de ruta asignados a un conductor según su identificación
    List<String> consultarRutasPorConductorIdentificacion(String identificacion);

    // Consulta las rutas y conductores asignados a un vehículo según su placa
    List<Map<String, Object>> consultarRutasPorVehiculoPlaca(String placa);

    // Consulta las rutas no operables (vehículo con documentos no habilitados o conductor en RO)
    List<Map<String, Object>> consultarRutasNoOperables();

    // Actualiza coordenadas geográficas de un trayecto
    Trayecto actualizarCoordenadas(Long id, Double latitud, Double longitud);

    // Obtiene todos los trayectos pendientes de geocodificación
    List<Trayecto> obtenerTrayectosSinCoordenadas();

    // Obtiene la lista de todos los códigos de ruta únicos registrados
    List<String> listarTodosLosCodigosRuta();
}

