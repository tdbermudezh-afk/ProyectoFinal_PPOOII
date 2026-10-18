package com.PPOOII.proyecto.services; // Define el paquete de servicios del proyecto

import com.PPOOII.proyecto.entities.Documento; // Importa la entidad paramétrica Documento
import com.PPOOII.proyecto.entities.Vehiculo; // Importa la entidad Vehiculo
import com.PPOOII.proyecto.entities.VehiculoDocumento; // Importa la entidad relacional VehiculoDocumento
import com.PPOOII.proyecto.repository.DocumentoRepository; // Importa el repositorio de Documento
import com.PPOOII.proyecto.repository.VehiculoDocumentoRepository; // Importa el repositorio de VehiculoDocumento
import com.PPOOII.proyecto.repository.VehiculoRepository; // Importa el repositorio de Vehiculo
import org.springframework.beans.factory.annotation.Autowired; // Importa la anotación para inyección de dependencias
import org.springframework.stereotype.Service; // Importa la anotación que marca la clase como un servicio de Spring
import org.springframework.transaction.annotation.Transactional; // Importa la anotación para manejo de transacciones

import java.time.LocalDate; // Importa LocalDate para el manejo de fechas de expedición y vencimiento
import java.util.ArrayList; // Importa ArrayList para construir la lista de documentos procesados
import java.util.List; // Importa la interfaz List
import java.util.Map; // Importa la interfaz Map para leer el payload JSON de cada documento

@Service // Registra la clase como un servicio dentro del contenedor de Spring
public class VehiculoDocumentoService { // Declaración de la clase VehiculoDocumentoService

    @Autowired // Inyecta automáticamente el repositorio de la relación vehículo-documento
    private VehiculoDocumentoRepository vehiculoDocumentoRepository; // Instancia de VehiculoDocumentoRepository

    @Autowired // Inyecta automáticamente el repositorio de vehículos
    private VehiculoRepository vehiculoRepository; // Instancia de VehiculoRepository

    @Autowired // Inyecta automáticamente el repositorio del catálogo de documentos
    private DocumentoRepository documentoRepository; // Instancia de DocumentoRepository

    @Transactional // Ejecuta todo el cargue dentro de una transacción atómica de base de datos
    public List<VehiculoDocumento> cargarDocumentos(List<Map<String, Object>> listaDocumentosPayload) { // Método para cargar o actualizar uno o varios documentos con PDF Base64
        if (listaDocumentosPayload == null || listaDocumentosPayload.isEmpty()) { // Verifica que la lista enviada en el body no sea nula ni esté vacía
            throw new IllegalArgumentException("Debe enviar al menos un documento para realizar el cargue."); // Lanza error 400 si no se envía ningún documento
        } // Fin de validación de lista vacía

        List<VehiculoDocumento> guardados = new ArrayList<>(); // Inicializa la lista donde se almacenarán los registros guardados

        for (Map<String, Object> item : listaDocumentosPayload) { // Recorre cada objeto de documento enviado en el arreglo JSON
            if (item.get("vehiculoId") == null || item.get("documentoId") == null) { // Verifica que se hayan enviado los IDs de vehículo y documento
                throw new IllegalArgumentException("Los campos 'vehiculoId' y 'documentoId' son obligatorios."); // Lanza error 400 si falta alguno de los IDs
            } // Fin de validación de IDs obligatorios

            int vehiculoId = Integer.parseInt(item.get("vehiculoId").toString()); // Convierte el identificador del vehículo a entero
            int documentoId = Integer.parseInt(item.get("documentoId").toString()); // Convierte el identificador del documento a entero
            String pdfBase64 = item.get("documentoPdfBase64") != null ? item.get("documentoPdfBase64").toString().trim() : null; // Extrae y limpia la cadena del PDF en Base64
            String fechaExpedicionStr = item.get("fechaExpedicion") != null ? item.get("fechaExpedicion").toString().trim() : null; // Extrae la fecha de expedición como texto
            String fechaVencimientoStr = item.get("fechaVencimiento") != null ? item.get("fechaVencimiento").toString().trim() : null; // Extrae la fecha de vencimiento como texto
            String estadoStr = item.get("estado") != null ? item.get("estado").toString().trim() : null; // Extrae el estado del documento como texto

            // Validar que el archivo PDF en Base64 sea obligatorio en este servicio (Entrega 2 - Punto 4)
            if (pdfBase64 == null || pdfBase64.isBlank()) { // Comprueba si no se subió el PDF en Base64 o si la cadena está vacía
                throw new IllegalArgumentException("El archivo PDF en formato Base64 ('documentoPdfBase64') es obligatorio y no puede estar vacío."); // Lanza error 400 indicando que falta el documento PDF
            } // Fin de validación de obligatoriedad del PDF en Base64

            Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId) // Busca el vehículo en la base de datos por su ID
                    .orElseThrow(() -> new RuntimeException("Vehículo no encontrado con ID: " + vehiculoId)); // Lanza excepción si el vehículo no existe

            Documento documento = documentoRepository.findById(documentoId) // Busca el tipo de documento en el catálogo por su ID
                    .orElseThrow(() -> new RuntimeException("Documento tipo catálogo no encontrado con ID: " + documentoId)); // Lanza excepción si el documento no existe

            // Validar compatibilidad entre el documento y el tipo de vehículo
            String tipoVehiculo = vehiculo.getTipoVehiculo(); // Obtiene el tipo de vehículo ('Automóvil' o 'Motocicleta')
            String aplica = documento.getAplicaA(); // Obtiene el código de aplicación del documento ('A', 'M' o 'AM')
            if ("Automóvil".equalsIgnoreCase(tipoVehiculo) && "M".equalsIgnoreCase(aplica)) { // Verifica si el vehículo es Automóvil pero el documento es exclusivo de Motocicleta
                throw new IllegalArgumentException("El documento '" + documento.getNombre() + "' solo aplica para Motocicletas."); // Lanza error 400 por incompatibilidad
            } // Fin de validación para Automóvil
            if ("Motocicleta".equalsIgnoreCase(tipoVehiculo) && "A".equalsIgnoreCase(aplica)) { // Verifica si el vehículo es Motocicleta pero el documento es exclusivo de Automóvil
                throw new IllegalArgumentException("El documento '" + documento.getNombre() + "' solo aplica para Automóviles."); // Lanza error 400 por incompatibilidad
            } // Fin de validación para Motocicleta

            // Buscar si ya existe la relación para actualizarla o crear un nuevo registro
            VehiculoDocumento vDoc = null; // Inicializa la referencia de la relación en null
            if (item.containsKey("id") && item.get("id") != null) { // Si el JSON incluye el ID específico de la relación vehiculo_documento
                int docRelId = Integer.parseInt(item.get("id").toString()); // Convierte el ID de la relación a entero
                vDoc = vehiculoDocumentoRepository.findById(docRelId).orElse(null); // Busca el registro existente por su clave primaria
            } // Fin de búsqueda por ID de relación
            if (vDoc == null) { // Si no se envió ID de relación o no se encontró por ID
                List<VehiculoDocumento> existentes = vehiculoDocumentoRepository.findByVehiculoId(vehiculoId); // Consulta todos los documentos actuales del vehículo
                vDoc = existentes.stream() // Abre un flujo sobre los documentos asociados al vehículo
                        .filter(d -> d.getDocumento().getId() == documentoId) // Filtra si ya tiene asociado este mismo tipo de documento
                        .findFirst() // Toma la primera coincidencia encontrada
                        .orElse(new VehiculoDocumento()); // Si no existe, instancia una nueva entidad VehiculoDocumento
            } // Fin de búsqueda por combinación vehículo y documento

            vDoc.setVehiculo(vehiculo); // Asocia la entidad Vehiculo al registro
            vDoc.setDocumento(documento); // Asocia la entidad Documento al registro

            if (fechaExpedicionStr != null && !fechaExpedicionStr.isEmpty()) { // Si se proporcionó fecha de expedición
                vDoc.setFechaExpedicion(LocalDate.parse(fechaExpedicionStr)); // Parsea y asigna la fecha de expedición
            } // Fin de asignación de fecha de expedición
            if (fechaVencimientoStr != null && !fechaVencimientoStr.isEmpty()) { // Si se proporcionó fecha de vencimiento
                vDoc.setFechaVencimiento(LocalDate.parse(fechaVencimientoStr)); // Parsea y asigna la fecha de vencimiento
            } // Fin de asignación de fecha de vencimiento

            if (vDoc.getFechaExpedicion() != null && vDoc.getFechaVencimiento() != null) { // Si ambas fechas están presentes
                if (vDoc.getFechaVencimiento().isBefore(vDoc.getFechaExpedicion())) { // Verifica si la fecha de vencimiento es anterior a la de expedición
                    throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la de expedición."); // Lanza error 400 si el rango de fechas es incoherente
                } // Fin de validación de coherencia de fechas
            } // Fin de verificación de fechas no nulas

            vDoc.setDocumentoPdfBase64(pdfBase64); // Asigna la cadena del archivo PDF en formato Base64 al campo BLOB/LONGTEXT

            if (estadoStr != null && !estadoStr.isEmpty()) { // Si se especificó un estado en el payload JSON
                if (!estadoStr.matches("^(Habilitado|Vencido|En Verificación)$")) { // Valida que el estado cumpla la restricción de chequeo
                    throw new IllegalArgumentException("Estado inválido. Debe ser: Habilitado, Vencido o En Verificación."); // Lanza error 400 si el estado no es válido
                } // Fin de validación de dominio de estado
                vDoc.setEstado(estadoStr); // Asigna el estado validado al registro
            } else if (vDoc.getEstado() == null) { // Si no se envió estado y es un registro nuevo sin estado previo
                vDoc.setEstado("En Verificación"); // Asigna por defecto el estado 'En Verificación'
            } // Fin de asignación de estado

            guardados.add(vehiculoDocumentoRepository.save(vDoc)); // Persiste el registro en base de datos y lo añade a la lista de resultados
        } // Fin del ciclo for sobre los documentos enviados

        return guardados; // Retorna la lista de documentos creados o actualizados
    } // Fin del método cargarDocumentos
} // Fin de la clase VehiculoDocumentoService