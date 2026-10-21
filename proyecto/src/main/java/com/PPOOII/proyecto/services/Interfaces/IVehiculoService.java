package com.PPOOII.proyecto.services.Interfaces; // Define el paquete de interfaces de servicio según el Laboratorio 1

import com.PPOOII.proyecto.entities.Vehiculo; // Importa la entidad Vehiculo
import com.PPOOII.proyecto.entities.VehiculoDocumento; // Importa la entidad VehiculoDocumento
import java.util.List; // Importa la interfaz List
import java.util.Optional; // Importa Optional para búsquedas opcionales

// Interfaz que define los métodos del servicio de Vehículo según la arquitectura del Laboratorio 1
public interface IVehiculoService { // Declaración de la interfaz IVehiculoService

    // MÉTODOS CRUD Y DE BÚSQUEDA DE VEHÍCULO
    List<Vehiculo> listarTodos(); // Consulta todos los vehículos registrados

    Vehiculo buscarPorId(int id); // Busca un vehículo por su identificador único

    Optional<Vehiculo> buscarPorPlaca(String placa); // Busca un vehículo por su número de placa

    List<Vehiculo> buscarPorTipoVehiculo(String tipoVehiculo); // Busca vehículos filtrados por su tipo

    List<Vehiculo> buscarPorTipoDocumento(int documentoId); // Busca vehículos que tengan en común un tipo de documento

    List<Vehiculo> buscarPorEstadoDocumento(String estado); // Busca vehículos según el estado de su documento asociado

    Vehiculo guardar(Vehiculo vehiculo); // Registra un nuevo vehículo con sus documentos asociados

    Optional<Vehiculo> actualizar(int id, Vehiculo vehiculoDetalles); // Actualiza un vehículo existente

    void eliminar(int id); // Elimina un vehículo por su identificador

    Optional<Vehiculo> agregarDocumentoAVehiculo(int vehiculoId, VehiculoDocumento nuevoDocumento); // Agrega un documento a un vehículo
} // Fin de la interfaz IVehiculoService

