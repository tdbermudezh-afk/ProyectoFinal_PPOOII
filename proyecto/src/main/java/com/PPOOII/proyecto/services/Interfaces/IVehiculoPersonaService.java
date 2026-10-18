package com.PPOOII.proyecto.services.Interfaces; // Define el paquete de interfaces de servicio según el Laboratorio 1

import com.PPOOII.proyecto.entities.VehiculoPersona; // Importa la entidad relacional VehiculoPersona
import java.util.List; // Importa la interfaz List

// Interfaz que define los métodos de asociación y cambio de estado entre Conductor y Vehículo
public interface IVehiculoPersonaService { // Declaración de la interfaz IVehiculoPersonaService

    // MÉTODOS DE ASIGNACIÓN Y GESTIÓN DE ESTADO DEL CONDUCTOR
    VehiculoPersona asociarConductorAVehiculo(Long personaId, Long vehiculoId, String estadoInicial); // Asocia un conductor a un vehículo

    VehiculoPersona cambiarEstadoConductor(Long idRelacion, String nuevoEstado); // Cambia el estado del conductor ('PO', 'EA', 'RO')

    List<VehiculoPersona> listarPorPersona(Long personaId); // Lista las asignaciones de un conductor específico
} // Fin de la interfaz IVehiculoPersonaService

