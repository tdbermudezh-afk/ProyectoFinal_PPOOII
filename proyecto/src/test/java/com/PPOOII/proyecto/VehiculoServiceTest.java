package com.PPOOII.proyecto;

import com.PPOOII.proyecto.entities.Documento;
import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.repository.DocumentoRepository;
import com.PPOOII.proyecto.repository.VehiculoRepository;
import com.PPOOII.proyecto.services.VehiculoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private DocumentoRepository documentoRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    private Documento documentoParam;

    @BeforeEach
    void setUp() {
        documentoParam = new Documento();
        documentoParam.setId(1);
        documentoParam.setCodigo("DOC001");
        documentoParam.setNombre("SOAT");
        documentoParam.setAplicaA("AM");
        documentoParam.setObligatorio("RR");
        documentoParam.setDescripcion("Seguro Obligatorio");
    }

    private Vehiculo crearVehiculoValidoAutomovil() {
        Vehiculo v = new Vehiculo();
        v.setId(1);
        v.setPlaca("ABC123");
        v.setTipoVehiculo("Automóvil");
        v.setMarca("Toyota");
        v.setModelo(2022);
        v.setTipoServicio("Pr");
        v.setTipoCombustible("Gasolina");
        v.setCapacidadPasajeros(5);
        v.setColor("#FFFFFF");
        v.setLinea("Corolla");

        VehiculoDocumento vd = new VehiculoDocumento();
        vd.setDocumento(documentoParam);
        vd.setFechaExpedicion(LocalDate.now());
        vd.setFechaVencimiento(LocalDate.now().plusYears(1));
        vd.setEstado("Habilitado"); // Se envía con otro estado para comprobar que el servicio lo cambia a 'En Verificación'

        List<VehiculoDocumento> docs = new ArrayList<>();
        docs.add(vd);
        v.setDocumentos(docs);

        return v;
    }

    @Test
    @DisplayName("Debe lanzar excepción si se intenta crear un vehículo sin documentos asociados")
    void testCrearVehiculoSinDocumentos_LanzaExcepcion() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("ABC123");
        vehiculo.setTipoVehiculo("Automóvil");
        vehiculo.setDocumentos(new ArrayList<>()); // Lista vacía

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            vehiculoService.guardar(vehiculo);
        });

        assertTrue(ex.getMessage().contains("No se puede crear un vehículo sin que tenga un documento asociado"));
        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el formato de placa de un automóvil no es 3 letras y 3 números")
    void testCrearAutomovilPlacaInvalida_LanzaExcepcion() {
        Vehiculo vehiculo = crearVehiculoValidoAutomovil();
        vehiculo.setPlaca("ABC12D"); // Formato de moto para un automóvil

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            vehiculoService.guardar(vehiculo);
        });

        assertTrue(ex.getMessage().contains("Para tipo Automóvil, la placa debe contener 3 letras seguidas de 3 números"));
        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el formato de placa de una motocicleta no es 3 letras, 2 números y 1 letra")
    void testCrearMotocicletaPlacaInvalida_LanzaExcepcion() {
        Vehiculo vehiculo = crearVehiculoValidoAutomovil();
        vehiculo.setTipoVehiculo("Motocicleta");
        vehiculo.setPlaca("ABC123"); // Formato de auto para una moto

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            vehiculoService.guardar(vehiculo);
        });

        assertTrue(ex.getMessage().contains("Para tipo Motocicleta, la placa debe contener 3 letras seguidas de 2 números y terminar en una letra"));
        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si ya existe un vehículo con la misma placa")
    void testCrearVehiculoPlacaDuplicada_LanzaExcepcion() {
        Vehiculo vehiculo = crearVehiculoValidoAutomovil();
        when(vehiculoRepository.existsByPlaca("ABC123")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            vehiculoService.guardar(vehiculo);
        });

        assertTrue(ex.getMessage().contains("Ya existe un vehículo registrado con la placa"));
        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe forzar estado 'En Verificación' en todos los documentos asociados al crear el vehículo")
    void testCrearVehiculoExitoso_FuerzaEstadoEnVerificacion() {
        Vehiculo vehiculo = crearVehiculoValidoAutomovil();

        when(vehiculoRepository.existsByPlaca("ABC123")).thenReturn(false);
        when(documentoRepository.findById(1)).thenReturn(Optional.of(documentoParam));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehiculo guardado = vehiculoService.guardar(vehiculo);

        assertNotNull(guardado);
        assertEquals("ABC123", guardado.getPlaca());
        assertEquals(1, guardado.getDocumentos().size());
        assertEquals("En Verificación", guardado.getDocumentos().get(0).getEstado());
        verify(vehiculoRepository, times(1)).save(vehiculo);
    }

    @Test
    @DisplayName("Debe buscar vehículos por tipo de vehículo")
    void testBuscarPorTipoVehiculo() {
        List<Vehiculo> lista = List.of(crearVehiculoValidoAutomovil());
        when(vehiculoRepository.findByTipoVehiculo("Automóvil")).thenReturn(lista);

        List<Vehiculo> resultado = vehiculoService.buscarPorTipoVehiculo("Automóvil");
        assertEquals(1, resultado.size());
        verify(vehiculoRepository).findByTipoVehiculo("Automóvil");
    }

    @Test
    @DisplayName("Debe buscar vehículos por documento en común")
    void testBuscarPorTipoDocumento() {
        List<Vehiculo> lista = List.of(crearVehiculoValidoAutomovil());
        when(vehiculoRepository.findByTipoDocumento(1)).thenReturn(lista);

        List<Vehiculo> resultado = vehiculoService.buscarPorTipoDocumento(1);
        assertEquals(1, resultado.size());
        verify(vehiculoRepository).findByTipoDocumento(1);
    }

    @Test
    @DisplayName("Debe buscar vehículos por estado del documento asociado")
    void testBuscarPorEstadoDocumento() {
        List<Vehiculo> lista = List.of(crearVehiculoValidoAutomovil());
        when(vehiculoRepository.findByEstadoDocumento("En Verificación")).thenReturn(lista);

        List<Vehiculo> resultado = vehiculoService.buscarPorEstadoDocumento("En Verificación");
        assertEquals(1, resultado.size());
        verify(vehiculoRepository).findByEstadoDocumento("En Verificación");
    }
}

