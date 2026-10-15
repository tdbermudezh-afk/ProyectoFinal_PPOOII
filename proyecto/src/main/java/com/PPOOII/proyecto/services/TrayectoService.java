// Paquete de servicios de la aplicación
package com.PPOOII.proyecto.services;

// Importaciones de entidades, repositorios y excepciones
import com.PPOOII.proyecto.entities.Persona;
import com.PPOOII.proyecto.entities.Trayecto;
import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.entities.VehiculoPersona;
import com.PPOOII.proyecto.exceptions.ResourceNotFoundException;
import com.PPOOII.proyecto.repository.PersonaRepository;
import com.PPOOII.proyecto.repository.TrayectoRepository;
import com.PPOOII.proyecto.repository.VehiculoDocumentoRepository;
import com.PPOOII.proyecto.repository.VehiculoPersonaRepository;
import com.PPOOII.proyecto.repository.VehiculoRepository;
import com.PPOOII.proyecto.services.Interfaces.ITrayectoService;

// Importaciones de Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importaciones de estructuras y utilidades de Java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Componente de servicio de Spring que implementa la interfaz ITrayectoService
@Service("trayectoService")
public class TrayectoService implements ITrayectoService {

    // Repositorio de trayectos
    @Autowired
    private TrayectoRepository trayectoRepository;

    // Repositorio de personas
    @Autowired
    private PersonaRepository personaRepository;

    // Repositorio de vehículos
    @Autowired
    private VehiculoRepository vehiculoRepository;

    // Repositorio de relación vehículo-documento
    @Autowired
    private VehiculoDocumentoRepository vehiculoDocumentoRepository;

    // Repositorio de relación vehículo-persona (conductor)
    @Autowired
    private VehiculoPersonaRepository vehiculoPersonaRepository;

    // Método transaccional para crear un nuevo trayecto validando las reglas de negocio de la Entrega 3
    @Override
    @Transactional
    public Trayecto crearTrayecto(Trayecto trayecto) {
        // 1. Validar que la persona exista y sea de tipo CONDUCTOR ('C')
        Long personaId = trayecto.getPersona() != null ? trayecto.getPersona().getId() : null;
        if (personaId == null) {
            throw new IllegalArgumentException("Debe especificar el ID del conductor.");
        }
        Persona conductor = personaRepository.findById(personaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con el ID: " + personaId));

        if (!"C".equalsIgnoreCase(conductor.getTipoPersona())) {
            throw new IllegalArgumentException("La persona asociada debe ser de tipo CONDUCTOR ('C').");
        }
        trayecto.setPersona(conductor);

        // 2. Validar que el vehículo exista en el sistema
        Integer vehiculoId = trayecto.getVehiculo() != null ? trayecto.getVehiculo().getId() : null;
        if (vehiculoId == null) {
            throw new IllegalArgumentException("Debe especificar el ID del vehículo.");
        }
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con el ID: " + vehiculoId));
        trayecto.setVehiculo(vehiculo);

        // 3. Regla de negocio: Verificar que TODOS los documentos del vehículo estén en estado 'Habilitado'
        List<VehiculoDocumento> documentosVehiculo = vehiculoDocumentoRepository.findByVehiculoId(vehiculoId);
        if (documentosVehiculo.isEmpty()) {
            throw new IllegalArgumentException("El vehículo no tiene documentos asociados y no puede realizar trayectos.");
        }
        boolean todosHabilitados = documentosVehiculo.stream()
                .allMatch(doc -> "Habilitado".equalsIgnoreCase(doc.getEstado()));
        if (!todosHabilitados) {
            throw new IllegalArgumentException("No se puede registrar el trayecto: Todos los documentos del vehículo deben estar en estado 'Habilitado'.");
        }

        // 4. Regla de negocio: Verificar que el conductor asociado esté en estado 'PO' (Puede Operar) con dicho vehículo
        List<VehiculoPersona> relaciones = vehiculoPersonaRepository.findByPersonaId(personaId);
        boolean conductorPuedeOperar = relaciones.stream()
                .anyMatch(rel -> rel.getVehiculo().getId() == vehiculoId && "PO".equalsIgnoreCase(rel.getEstadoConductor()));

        if (!conductorPuedeOperar) {
            throw new IllegalArgumentException("No se puede registrar el trayecto: El conductor no cuenta con estado 'PO' (Puede Operar) para este vehículo.");
        }

        // 5. Validar regla de paradas: máximo 5 paradas intermedias + inicial + final = máx 7 paradas por ruta
        long totalParadasPrevias = trayectoRepository.countByCodigoRuta(trayecto.getCodigoRuta());
        if (totalParadasPrevias >= 7) {
            throw new IllegalArgumentException("La ruta '" + trayecto.getCodigoRuta() + "' ya alcanzó el límite máximo de paradas permitidas (7).");
        }

        // 6. Persistir el trayecto en la base de datos
        return trayectoRepository.save(trayecto);
    }

    // Consulta de rutas por código de ruta ordenadas secuencialmente
    @Override
    @Transactional(readOnly = true)
    public List<Trayecto> consultarRutaPorCodigo(String codigoRuta) {
        List<Trayecto> trayectos = trayectoRepository.findByCodigoRutaOrderByOrdenParadaAsc(codigoRuta);
        if (trayectos.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron trayectos registrados para el código de ruta: " + codigoRuta);
        }
        return trayectos;
    }

    // Consulta de códigos de ruta asignados a un conductor según su identificación
    @Override
    @Transactional(readOnly = true)
    public List<String> consultarRutasPorConductorIdentificacion(String identificacion) {
        List<String> codigos = trayectoRepository.findCodigosRutaByPersonaIdentificacion(identificacion);
        if (codigos.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron rutas asociadas al conductor con identificación: " + identificacion);
        }
        return codigos;
    }

    // Consulta de rutas y conductor asociado por la placa de un vehículo
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> consultarRutasPorVehiculoPlaca(String placa) {
        List<Object[]> resultados = trayectoRepository.findRutasYConductorByVehiculoPlaca(placa);
        List<Map<String, Object>> respuesta = new ArrayList<>();

        for (Object[] fila : resultados) {
            Map<String, Object> item = new HashMap<>();
            item.put("codigoRuta", fila[0]);
            item.put("nombresConductor", fila[1]);
            item.put("apellidosConductor", fila[2]);
            item.put("identificacionConductor", fila[3]);
            respuesta.add(item);
        }

        return respuesta;
    }

    // Consulta de rutas no operables: vehículo con documentos no habilitados o conductor en RO
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> consultarRutasNoOperables() {
        List<String> todosCodigos = trayectoRepository.findDistinctCodigosRuta();
        List<Map<String, Object>> noOperables = new ArrayList<>();

        for (String codigo : todosCodigos) {
            List<Trayecto> paradas = trayectoRepository.findByCodigoRutaOrderByOrdenParadaAsc(codigo);
            if (paradas.isEmpty()) continue;

            Trayecto primerPunto = paradas.get(0);
            Vehiculo v = primerPunto.getVehiculo();
            Persona c = primerPunto.getPersona();

            // Verificar si el vehículo tiene algún documento que NO esté 'Habilitado'
            List<VehiculoDocumento> docs = vehiculoDocumentoRepository.findByVehiculoId(v.getId());
            boolean vehiculoInhabilitado = docs.isEmpty() || docs.stream().anyMatch(d -> !"Habilitado".equalsIgnoreCase(d.getEstado()));

            // Verificar si el conductor está en estado 'RO' para ese vehículo
            List<VehiculoPersona> rels = vehiculoPersonaRepository.findByPersonaId(c.getId());
            boolean conductorRestringido = rels.stream()
                    .anyMatch(r -> r.getVehiculo().getId() == v.getId() && "RO".equalsIgnoreCase(r.getEstadoConductor()));

            if (vehiculoInhabilitado || conductorRestringido) {
                Map<String, Object> rutaInfo = new HashMap<>();
                rutaInfo.put("codigoRuta", codigo);
                rutaInfo.put("vehiculoPlaca", v.getPlaca());
                rutaInfo.put("conductorIdentificacion", c.getIdentificacion());
                rutaInfo.put("conductorNombre", c.getNombres() + " " + c.getApellidos());
                rutaInfo.put("motivoInoperable", vehiculoInhabilitado && conductorRestringido 
                        ? "Vehículo con documentos no habilitados Y Conductor en estado RO"
                        : (vehiculoInhabilitado ? "Vehículo con documentos no habilitados o vencidos" : "Conductor restringido para operar (RO)"));
                rutaInfo.put("totalParadas", paradas.size());
                rutaInfo.put("trayectos", paradas);
                noOperables.add(rutaInfo);
            }
        }

        return noOperables;
    }

    // Actualización de coordenadas para un trayecto
    @Override
    @Transactional
    public Trayecto actualizarCoordenadas(Long id, Double latitud, Double longitud) {
        Trayecto t = trayectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trayecto no encontrado con ID: " + id));
        t.setLatitud(latitud);
        t.setLongitud(longitud);
        return trayectoRepository.save(t);
    }

    // Obtener trayectos sin coordenadas para la tarea programada
    @Override
    @Transactional(readOnly = true)
    public List<Trayecto> obtenerTrayectosSinCoordenadas() {
        return trayectoRepository.findTrayectosSinCoordenadas();
    }

    // Listar todos los códigos de ruta únicos registrados
    @Override
    @Transactional(readOnly = true)
    public List<String> listarTodosLosCodigosRuta() {
        return trayectoRepository.findDistinctCodigosRuta();
    }
}

