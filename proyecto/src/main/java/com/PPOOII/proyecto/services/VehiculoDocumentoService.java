package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.entities.Documento;
import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.repository.DocumentoRepository;
import com.PPOOII.proyecto.repository.VehiculoDocumentoRepository;
import com.PPOOII.proyecto.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VehiculoDocumentoService {

    @Autowired
    private VehiculoDocumentoRepository vehiculoDocumentoRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Transactional
    public List<VehiculoDocumento> cargarDocumentos(List<Map<String, Object>> listaDocumentosPayload) {
        List<VehiculoDocumento> guardados = new ArrayList<>();

        for (Map<String, Object> item : listaDocumentosPayload) {
            int vehiculoId = Integer.parseInt(item.get("vehiculoId").toString());
            int documentoId = Integer.parseInt(item.get("documentoId").toString());
            String pdfBase64 = item.get("documentoPdfBase64") != null ? item.get("documentoPdfBase64").toString() : null;
            String fechaExpedicionStr = item.get("fechaExpedicion") != null ? item.get("fechaExpedicion").toString() : null;
            String fechaVencimientoStr = item.get("fechaVencimiento") != null ? item.get("fechaVencimiento").toString() : null;
            String estadoStr = item.get("estado") != null ? item.get("estado").toString().trim() : null;

            Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                    .orElseThrow(() -> new RuntimeException("Vehículo no encontrado con ID: " + vehiculoId));

            Documento documento = documentoRepository.findById(documentoId)
                    .orElseThrow(() -> new RuntimeException("Documento tipo catálogo no encontrado con ID: " + documentoId));

            // Validar compatibilidad documento y tipo de vehículo
            String tipoVehiculo = vehiculo.getTipoVehiculo();
            String aplica = documento.getAplicaA();
            if ("Automóvil".equalsIgnoreCase(tipoVehiculo) && "M".equalsIgnoreCase(aplica)) {
                throw new IllegalArgumentException("El documento '" + documento.getNombre() + "' solo aplica para Motocicletas.");
            }
            if ("Motocicleta".equalsIgnoreCase(tipoVehiculo) && "A".equalsIgnoreCase(aplica)) {
                throw new IllegalArgumentException("El documento '" + documento.getNombre() + "' solo aplica para Automóviles.");
            }

            // Buscar si ya existe para actualizar o crear nuevo
            VehiculoDocumento vDoc = null;
            if (item.containsKey("id") && item.get("id") != null) {
                int docRelId = Integer.parseInt(item.get("id").toString());
                vDoc = vehiculoDocumentoRepository.findById(docRelId).orElse(null);
            }
            if (vDoc == null) {
                List<VehiculoDocumento> existentes = vehiculoDocumentoRepository.findByVehiculoId(vehiculoId);
                vDoc = existentes.stream()
                        .filter(d -> d.getDocumento().getId() == documentoId)
                        .findFirst()
                        .orElse(new VehiculoDocumento());
            }

            vDoc.setVehiculo(vehiculo);
            vDoc.setDocumento(documento);

            if (fechaExpedicionStr != null) {
                vDoc.setFechaExpedicion(LocalDate.parse(fechaExpedicionStr));
            }
            if (fechaVencimientoStr != null) {
                vDoc.setFechaVencimiento(LocalDate.parse(fechaVencimientoStr));
            }

            if (vDoc.getFechaExpedicion() != null && vDoc.getFechaVencimiento() != null) {
                if (vDoc.getFechaVencimiento().isBefore(vDoc.getFechaExpedicion())) {
                    throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la de expedición.");
                }
            }

            if (pdfBase64 != null) {
                vDoc.setDocumentoPdfBase64(pdfBase64);
            }

            if (estadoStr != null && !estadoStr.isEmpty()) {
                if (!estadoStr.matches("^(Habilitado|Vencido|En Verificación)$")) {
                    throw new IllegalArgumentException("Estado inválido. Debe ser: Habilitado, Vencido o En Verificación.");
                }
                vDoc.setEstado(estadoStr);
            } else if (vDoc.getEstado() == null) {
                vDoc.setEstado("En Verificación");
            }

            guardados.add(vehiculoDocumentoRepository.save(vDoc));
        }

        return guardados;
    }
}