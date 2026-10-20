// Paquete de servicios de lógica de negocio de la aplicación
package com.PPOOII.proyecto.services;

// Importación de la entidad paramétrica Documento
import com.PPOOII.proyecto.entities.Documento;
// Importación de la entidad Vehiculo
import com.PPOOII.proyecto.entities.Vehiculo;
// Importación de la entidad de relación VehiculoDocumento
import com.PPOOII.proyecto.entities.VehiculoDocumento;
// Importación de excepción personalizada ResourceNotFoundException
import com.PPOOII.proyecto.exceptions.ResourceNotFoundException;
// Importación del repositorio JPA de Documento
import com.PPOOII.proyecto.repository.DocumentoRepository;
// Importación del repositorio JPA de VehiculoDocumento
import com.PPOOII.proyecto.repository.VehiculoDocumentoRepository;
// Importación del repositorio JPA de Vehiculo
import com.PPOOII.proyecto.repository.VehiculoRepository;
// Importación de la interfaz de servicio IVehiculoDocumentoService (Laboratorio 1)
import com.PPOOII.proyecto.services.Interfaces.IVehiculoDocumentoService;
// Importación del gestor de logs Log4j2 (Laboratorio 1)
import org.apache.logging.log4j.LogManager;
// Importación de la interfaz Logger de Log4j2 (Laboratorio 1)
import org.apache.logging.log4j.Logger;
// Importación de la anotación @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de la anotación @Service de Spring
import org.springframework.stereotype.Service;
// Importación de la anotación @Transactional para manejo transaccional
import org.springframework.transaction.annotation.Transactional;
// Importación de MultipartFile para recepción de archivos PDF
import org.springframework.web.multipart.MultipartFile;

// Importación de IOException para lectura de bytes del PDF
import java.io.IOException;
// Importación de la clase LocalDate para manejo de fechas
import java.time.LocalDate;
// Importación de ArrayList para construir listas dinámicas
import java.util.ArrayList;
// Importación de Base64 para codificar archivos PDF binarios a Base64
import java.util.Base64;
// Importación de HashMap para construcción de payloads
import java.util.HashMap;
// Importación de la interfaz List de Java
import java.util.List;
// Importación de la interfaz Map de Java
import java.util.Map;
// Importación de Collectors para filtrado de flujos
import java.util.stream.Collectors;

// Servicio que implementa IVehiculoDocumentoService para la carga y gestión de documentos PDF Base64 de vehículos
@Service("VehiculoDocumentoService")
public class VehiculoDocumentoService implements IVehiculoDocumentoService {

    // Instancia del logger de auditoría según el estándar del Laboratorio 1
    private static final Logger logger = LogManager.getLogger(VehiculoDocumentoService.class);

    // Inyección del repositorio de relación Vehículo-Documento
    @Autowired
    private VehiculoDocumentoRepository vehiculoDocumentoRepository;

    // Inyección del repositorio de Vehículos
    @Autowired
    private VehiculoRepository vehiculoRepository;

    // Inyección del repositorio de Documentos parametrizados
    @Autowired
    private DocumentoRepository documentoRepository;

    // Procesa la carga masiva o individual de documentos PDF en formato Base64 asociados a vehículos
    @Override
    @Transactional
    public List<VehiculoDocumento> cargarDocumentos(List<Map<String, Object>> listaDocumentosPayload) {
        if (listaDocumentosPayload == null || listaDocumentosPayload.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un documento en la lista para realizar el cargue.");
        }

        // Lista donde se almacenarán los registros procesados y guardados
        List<VehiculoDocumento> guardados = new ArrayList<>();

        // Itera sobre cada elemento enviado en el payload JSON
        for (Map<String, Object> item : listaDocumentosPayload) {
            if (item.get("vehiculoId") == null || item.get("documentoId") == null) {
                throw new IllegalArgumentException("Cada registro debe incluir 'vehiculoId' y 'documentoId'.");
            }
            // Extrae y convierte el ID del vehículo
            int vehiculoId = Integer.parseInt(item.get("vehiculoId").toString());
            // Extrae y convierte el ID del documento parametrizado
            int documentoId = Integer.parseInt(item.get("documentoId").toString());
            // Extrae la cadena del documento PDF codificado en Base64
            String pdfBase64 = item.get("documentoPdfBase64") != null ? item.get("documentoPdfBase64").toString() : null;
            // Extrae la fecha de expedición en formato texto (YYYY-MM-DD)
            String fechaExpedicionStr = item.get("fechaExpedicion") != null ? item.get("fechaExpedicion").toString() : null;
            // Extrae la fecha de vencimiento en formato texto (YYYY-MM-DD)
            String fechaVencimientoStr = item.get("fechaVencimiento") != null ? item.get("fechaVencimiento").toString() : null;
            // Extrae el estado del documento si fue especificado
            String estadoStr = item.get("estado") != null ? item.get("estado").toString().trim() : null;

            // Validación obligatoria: Verifica que se haya adjuntado el contenido del PDF en Base64
            if (pdfBase64 == null || pdfBase64.trim().isEmpty()) {
                // Lanza excepción si no se subió el archivo PDF en Base64
                throw new IllegalArgumentException("El documento en formato PDF Base64 ('documentoPdfBase64') es obligatorio para realizar el cargue.");
            }

            // Busca el vehículo en la base de datos o lanza excepción si no existe
            Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                    // Mensaje de error si el vehículo no existe
                    .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + vehiculoId));

            // Busca el tipo de documento en el catálogo o lanza excepción si no existe
            Documento documento = documentoRepository.findById(documentoId)
                    // Mensaje de error si el documento del catálogo no existe
                    .orElseThrow(() -> new ResourceNotFoundException("Documento tipo catálogo no encontrado con ID: " + documentoId));

            // Validar compatibilidad entre el documento y el tipo de vehículo
            String tipoVehiculo = vehiculo.getTipoVehiculo();
            // Obtiene la aplicabilidad del documento ('A', 'M' o 'AM')
            String aplica = documento.getAplicaA();
            // Rechaza si el vehículo es Automóvil y el documento es exclusivo de Motocicletas
            if ("Automóvil".equalsIgnoreCase(tipoVehiculo) && "M".equalsIgnoreCase(aplica)) {
                // Lanza excepción de incompatibilidad
                throw new IllegalArgumentException("El documento '" + documento.getNombre() + "' solo aplica para Motocicletas.");
            }
            // Rechaza si el vehículo es Motocicleta y el documento es exclusivo de Automóviles
            if ("Motocicleta".equalsIgnoreCase(tipoVehiculo) && "A".equalsIgnoreCase(aplica)) {
                // Lanza excepción de incompatibilidad
                throw new IllegalArgumentException("El documento '" + documento.getNombre() + "' solo aplica para Automóviles.");
            }

            // Buscar si ya existe la relación para actualizarla o crear una nueva
            VehiculoDocumento vDoc = null;
            // Si el payload incluye el ID de la relación, intenta recuperarla directamente
            if (item.containsKey("id") && item.get("id") != null) {
                // Convierte el ID de la relación a entero
                int docRelId = Integer.parseInt(item.get("id").toString());
                // Busca el registro existente por ID
                vDoc = vehiculoDocumentoRepository.findById(docRelId).orElse(null);
            }
            // Si no se encontró por ID de relación, busca por coincidencia de vehículo y tipo de documento
            if (vDoc == null) {
                // Obtiene los documentos actuales del vehículo
                List<VehiculoDocumento> existentes = vehiculoDocumentoRepository.findByVehiculoId(vehiculoId);
                // Filtra por el ID del documento parametrizado o instancia uno nuevo
                vDoc = existentes.stream()
                        // Filtra los registros que coincidan con el documentoId
                        .filter(d -> d.getDocumento().getId() == documentoId)
                        // Toma la primera coincidencia
                        .findFirst()
                        // Si no existe coincidencia previa, crea una nueva instancia
                        .orElse(new VehiculoDocumento());
            }

            // Asigna la referencia al vehículo
            vDoc.setVehiculo(vehiculo);
            // Asigna la referencia al documento parametrizado
            vDoc.setDocumento(documento);

            // Si se envió fecha de expedición, la parsea y asigna
            if (fechaExpedicionStr != null) {
                // Convierte la cadena ISO a LocalDate
                vDoc.setFechaExpedicion(LocalDate.parse(fechaExpedicionStr));
            }
            // Si se envió fecha de vencimiento, la parsea y asigna
            if (fechaVencimientoStr != null) {
                // Convierte la cadena ISO a LocalDate
                vDoc.setFechaVencimiento(LocalDate.parse(fechaVencimientoStr));
            }

            // Verifica congruencia cronológica entre fecha de expedición y fecha de vencimiento
            if (vDoc.getFechaExpedicion() != null && vDoc.getFechaVencimiento() != null) {
                // Comprueba si la fecha de vencimiento es anterior a la de expedición
                if (vDoc.getFechaVencimiento().isBefore(vDoc.getFechaExpedicion())) {
                    // Lanza excepción cuando el rango de fechas es inválido
                    throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la de expedición.");
                }
            }

            // Asigna el contenido del documento PDF codificado en Base64 (almacenado en LONGBLOB)
            vDoc.setDocumentoPdfBase64(pdfBase64.trim());

            // Valida y asigna el estado del documento
            if (estadoStr != null && !estadoStr.isEmpty()) {
                // Verifica que el estado sea uno de los tres permitidos por la regla de negocio
                if (!estadoStr.matches("^(Habilitado|Vencido|En Verificación)$")) {
                    // Lanza excepción si el estado no es válido
                    throw new IllegalArgumentException("Estado inválido. Debe ser: Habilitado, Vencido o En Verificación.");
                }
                // Asigna el estado validado
                vDoc.setEstado(estadoStr);
            } else if (vDoc.getEstado() == null || vDoc.getEstado().isBlank()) {
                // Si no tenía estado previo, asigna 'En Verificación' por defecto
                vDoc.setEstado("En Verificación");
            }

            // Registra en el log el cargue exitoso del documento PDF Base64
            logger.info("Cargando documento PDF Base64 '{}' para el vehículo ID: {}", documento.getNombre(), vehiculoId);
            // Persiste el registro y lo agrega a la lista de resultados
            guardados.add(vehiculoDocumentoRepository.save(vDoc));
        }

        // Retorna la lista de documentos procesados y almacenados
        return guardados;
    }

    // Recibe un archivo físico PDF (MultipartFile), lo convierte a Base64 y lo guarda en el campo LONGBLOB
    @Override
    @Transactional
    public VehiculoDocumento cargarArchivoPdf(int vehiculoId, int documentoId, String fechaExpedicion, String fechaVencimiento, String estado, MultipartFile archivoPdf) {
        if (archivoPdf == null || archivoPdf.isEmpty()) {
            throw new IllegalArgumentException("Debe adjuntar un archivo PDF válido no vacío.");
        }
        try {
            byte[] pdfBytes = archivoPdf.getBytes();
            String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

            Map<String, Object> item = new HashMap<>();
            item.put("vehiculoId", vehiculoId);
            item.put("documentoId", documentoId);
            item.put("fechaExpedicion", fechaExpedicion != null ? fechaExpedicion : LocalDate.now().toString());
            item.put("fechaVencimiento", fechaVencimiento != null ? fechaVencimiento : LocalDate.now().plusYears(1).toString());
            item.put("estado", estado != null ? estado : "En Verificación");
            item.put("documentoPdfBase64", pdfBase64);

            List<VehiculoDocumento> resultado = cargarDocumentos(List.of(item));
            return resultado.get(0);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer y convertir el archivo PDF a Base64: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public VehiculoDocumento cambiarEstadoDocumento(int idVehiculoDocumento, String nuevoEstado) {
        if (nuevoEstado == null || !nuevoEstado.trim().matches("^(Habilitado|Vencido|En Verificación)$")) {
            throw new IllegalArgumentException("El estado del documento debe ser 'Habilitado', 'Vencido' o 'En Verificación'.");
        }
        VehiculoDocumento vDoc = vehiculoDocumentoRepository.findById(idVehiculoDocumento)
                .orElseThrow(() -> new ResourceNotFoundException("Documento asociado no encontrado con ID: " + idVehiculoDocumento));
        vDoc.setEstado(nuevoEstado.trim());
        logger.info("Actualizando estado de vehiculo_documento ID: {} a '{}'", idVehiculoDocumento, nuevoEstado);
        return vehiculoDocumentoRepository.save(vDoc);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehiculo> obtenerVehiculosConDocumentosVencidos() {
        return vehiculoDocumentoRepository.findVehiculosConDocumentosVencidos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehiculo> obtenerVehiculosConDocumentosPorVencer(int diasFaltantes) {
        LocalDate fechaLimite = LocalDate.now().plusDays(diasFaltantes);
        return vehiculoDocumentoRepository.findVehiculosConDocumentosPorVencer(fechaLimite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoDocumento> obtenerDocumentosVencidos() {
        LocalDate hoy = LocalDate.now();
        return vehiculoDocumentoRepository.findAll().stream()
                .filter(doc -> (doc.getFechaVencimiento() != null && doc.getFechaVencimiento().isBefore(hoy))
                        || "Vencido".equalsIgnoreCase(doc.getEstado()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoDocumento> obtenerDocumentosPorVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);
        return vehiculoDocumentoRepository.findAll().stream()
                .filter(doc -> doc.getFechaVencimiento() != null &&
                        !doc.getFechaVencimiento().isBefore(hoy) &&
                        !doc.getFechaVencimiento().isAfter(limite))
                .collect(Collectors.toList());
    }
}
