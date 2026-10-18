package com.PPOOII.proyecto.services.Interfaces; // Define el paquete de interfaces de servicio según el Laboratorio 1

import com.PPOOII.proyecto.entities.VehiculoDocumento; // Importa la entidad VehiculoDocumento
import java.util.List; // Importa la interfaz List
import java.util.Map; // Importa la interfaz Map

// Interfaz que define los métodos para el cargue y actualización de documentos de vehículos en Base64
public interface IVehiculoDocumentoService { // Declaración de la interfaz IVehiculoDocumentoService

    // MÉTODO DE CARGUE MASIVO / INDIVIDUAL DE DOCUMENTOS PDF EN BASE64
    List<VehiculoDocumento> cargarDocumentos(List<Map<String, Object>> listaDocumentosPayload); // Carga o actualiza documentos con PDF Base64
} // Fin de la interfaz IVehiculoDocumentoService

