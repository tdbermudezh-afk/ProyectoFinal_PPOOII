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
            String pdfBase64 = (String) item.get("documentoPdfBase64");
            String fechaExpedicionStr = (String) item.get("fechaExpedicion");
            String fechaVencimientoStr = (String) item.get("fechaVencimiento");

            Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                    .orElseThrow(() -> new RuntimeException("Vehículo no encontrado con ID: " + vehiculoId));

            Documento documento = documentoRepository.findById(documentoId)
                    .orElseThrow(() -> new RuntimeException("Documento tipo catálogo no encontrado con ID: " + documentoId));

            VehiculoDocumento vDoc = new VehiculoDocumento();
            vDoc.setVehiculo(vehiculo);
            vDoc.setDocumento(documento);
            vDoc.setFechaExpedicion(LocalDate.parse(fechaExpedicionStr));
            vDoc.setFechaVencimiento(LocalDate.parse(fechaVencimientoStr));
            vDoc.setDocumentoPdfBase64(pdfBase64);

            guardados.add(vehiculoDocumentoRepository.save(vDoc));
        }

        return guardados;
    }
}