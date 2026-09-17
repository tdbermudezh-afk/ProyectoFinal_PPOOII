package com.PPOOII.proyecto;

import com.PPOOII.proyecto.entities.Documento;
import com.PPOOII.proyecto.repository.DocumentoRepository;
import com.PPOOII.proyecto.services.DocumentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @InjectMocks
    private DocumentoService documentoService;

    private Documento doc;

    @BeforeEach
    void setUp() {
        doc = new Documento();
        doc.setId(1);
        doc.setCodigo("DOC001");
        doc.setNombre("SOAT");
        doc.setAplicaA("AM");
        doc.setObligatorio("RR");
        doc.setDescripcion("Seguro Obligatorio de Accidentes de Tránsito");
    }

    @Test
    @DisplayName("Debe lanzar excepción si se intenta guardar un documento con código ya existente")
    void testGuardarDocumentoCodigoDuplicado_LanzaExcepcion() {
        when(documentoRepository.existsByCodigo("DOC001")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            documentoService.guardar(doc);
        });

        assertTrue(ex.getMessage().contains("Ya existe un documento parametrizado con el código"));
        verify(documentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe guardar exitosamente un documento cuando el código es único")
    void testGuardarDocumentoExitoso() {
        when(documentoRepository.existsByCodigo("DOC001")).thenReturn(false);
        when(documentoRepository.save(any(Documento.class))).thenReturn(doc);

        Documento guardado = documentoService.guardar(doc);
        assertNotNull(guardado);
        assertEquals("DOC001", guardado.getCodigo());
        verify(documentoRepository, times(1)).save(doc);
    }

    @Test
    @DisplayName("Debe actualizar documento parametrizado correctamente")
    void testActualizarDocumentoExitoso() {
        Documento detalles = new Documento();
        detalles.setCodigo("DOC001");
        detalles.setNombre("SOAT Modificado");
        detalles.setAplicaA("A");
        detalles.setObligatorio("RA");
        detalles.setDescripcion("Nueva descripción");

        when(documentoRepository.findById(1)).thenReturn(Optional.of(doc));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(i -> i.getArgument(0));

        Optional<Documento> resultado = documentoService.actualizar(1, detalles);
        assertTrue(resultado.isPresent());
        assertEquals("SOAT Modificado", resultado.get().getNombre());
        assertEquals("A", resultado.get().getAplicaA());
    }

    @Test
    @DisplayName("Debe eliminar documento parametrizado")
    void testEliminarDocumentoExitoso() {
        when(documentoRepository.findById(1)).thenReturn(Optional.of(doc));

        boolean eliminado = documentoService.eliminar(1);
        assertTrue(eliminado);
        verify(documentoRepository).delete(doc);
    }
}

