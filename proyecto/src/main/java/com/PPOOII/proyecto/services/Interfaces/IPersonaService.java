package com.PPOOII.proyecto.services.Interfaces; // Define el paquete de interfaces de servicio según el Laboratorio 1

import com.PPOOII.proyecto.entities.Persona; // Importa la entidad Persona
import java.util.List; // Importa la interfaz List

// Interfaz que define los métodos del servicio de Persona según la arquitectura del Laboratorio 1
public interface IPersonaService { // Declaración de la interfaz IPersonaService

    // MÉTODOS CRUD Y DE CONSULTA DE PERSONA
    Persona crearPersona(Persona persona); // Registra una nueva persona (y autogenera su usuario si es Administrativo)

    Persona actualizarPersona(Long id, Persona detalles); // Actualiza los datos de una persona existente

    List<Persona> listarTodas(); // Consulta todas las personas registradas

    Persona obtenerPorId(Long id); // Busca una persona por su ID primario

    List<Persona> listarPorTipo(String tipoPersona); // Lista las personas según su tipo ('A' o 'C')
} // Fin de la interfaz IPersonaService

