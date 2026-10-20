package com.PPOOII.proyecto.services.Interfaces; // Define el paquete de interfaces de servicio según el Laboratorio 1

import com.PPOOII.proyecto.entities.Vehiculo; // Importa la entidad Vehiculo
import com.PPOOII.proyecto.entities.VehiculoDocumento; // Importa la entidad VehiculoDocumento
import org.springframework.web.multipart.MultipartFile; // Importa MultipartFile para subida de archivos PDF
import java.util.List; // Importa la interfaz List
import java.util.Map; // Importa la interfaz Map

// Interfaz que define los métodos para el cargue, conversión a Base64 y consulta de documentos de vehículos
public interface IVehiculoDocumentoService { // Declaración de la interfaz IVehiculoDocumentoService

    // MÉTODO DE CARGUE MASIVO / INDIVIDUAL DE DOCUMENTOS PDF EN BASE64
    List<VehiculoDocumento> cargarDocumentos(List<Map<String, Object>> listaDocumentosPayload); // Carga o actualiza documentos con PDF Base64

    // MÉTODO PARA SUBIR ARCHIVO PDF REAL (MultipartFile) Y CONVERTIRLO AUTOMÁTICAMENTE A BASE64
    VehiculoDocumento cargarArchivoPdf(int vehiculoId, int documentoId, String fechaExpedicion, String fechaVencimiento, String estado, MultipartFile archivoPdf);

    // MÉTODO PARA CAMBIAR EL ESTADO DE UN DOCUMENTO ASOCIADO A UN VEHÍCULO ('Habilitado', 'Vencido', 'En Verificación')
    VehiculoDocumento cambiarEstadoDocumento(int idVehiculoDocumento, String nuevoEstado);

    // CONSULTAS PÚBLICAS DE VEHÍCULOS SEGÚN VIGENCIA DE DOCUMENTOS (Entrega 2)
    List<Vehiculo> obtenerVehiculosConDocumentosVencidos();

    List<Vehiculo> obtenerVehiculosConDocumentosPorVencer(int diasFaltantes);

    List<VehiculoDocumento> obtenerDocumentosVencidos();

    List<VehiculoDocumento> obtenerDocumentosPorVencer(int dias);
} // Fin de la interfaz IVehiculoDocumentoService
