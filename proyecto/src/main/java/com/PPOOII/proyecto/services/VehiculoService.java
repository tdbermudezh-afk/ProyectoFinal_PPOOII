package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.entities.Documento;
import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.exceptions.ResourceNotFoundException;
import com.PPOOII.proyecto.repository.DocumentoRepository;
import com.PPOOII.proyecto.repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final DocumentoRepository documentoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository, DocumentoRepository documentoRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.documentoRepository = documentoRepository;
    }

    // Listar todos los vehículos
    public List<Vehiculo> listarTodos() {
        return vehiculoRepository.findAll();
    }

    // Buscar vehículo por su ID
    public Vehiculo buscarPorId(int id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con el ID: " + id));
    }

    // Buscar vehículo por número de placa
    public Optional<Vehiculo> buscarPorPlaca(String placa) {
        return vehiculoRepository.findByPlaca(placa);
    }

    // Buscar vehículos por tipo de vehículo (Automóvil / Motocicleta)
    public List<Vehiculo> buscarPorTipoVehiculo(String tipoVehiculo) {
        return vehiculoRepository.findByTipoVehiculo(tipoVehiculo);
    }

    // Buscar vehículos que tengan en común un tipo de documento
    public List<Vehiculo> buscarPorTipoDocumento(int documentoId) {
        return vehiculoRepository.findByTipoDocumento(documentoId);
    }

    // Buscar vehículos según el estado del documento (Habilitado, Vencido, En Verificación)
    public List<Vehiculo> buscarPorEstadoDocumento(String estado) {
        return vehiculoRepository.findByEstadoDocumento(estado);
    }

    // Validación del formato de placa según el tipo de vehículo
    private void validarPlacaPorTipo(String placa, String tipoVehiculo) {
        if (placa == null || placa.trim().length() != 6) {
            throw new IllegalArgumentException("La placa debe tener exactamente 6 caracteres.");
        }
        String placaUpper = placa.trim().toUpperCase();
        if ("Automóvil".equalsIgnoreCase(tipoVehiculo)) {
            // Automóvil: 3 letras seguidas de 3 números
            if (!placaUpper.matches("^[A-Z]{3}[0-9]{3}$")) {
                throw new IllegalArgumentException("Para tipo Automóvil, la placa debe contener 3 letras seguidas de 3 números (ejemplo: ABC123).");
            }
        } else if ("Motocicleta".equalsIgnoreCase(tipoVehiculo)) {
            // Motocicleta: 3 letras seguidas de 2 números y terminar en 1 letra
            if (!placaUpper.matches("^[A-Z]{3}[0-9]{2}[A-Z]$")) {
                throw new IllegalArgumentException("Para tipo Motocicleta, la placa debe contener 3 letras seguidas de 2 números y terminar en una letra (ejemplo: ABC12D).");
            }
        } else {
            throw new IllegalArgumentException("El tipo de vehículo debe ser 'Automóvil' o 'Motocicleta'.");
        }
    }

    // Guardar nuevo vehículo con validaciones de negocio
    public Vehiculo guardar(Vehiculo vehiculo) {
        // Regla: No se puede crear un vehículo sin que tenga al menos un documento asociado
        if (vehiculo.getDocumentos() == null || vehiculo.getDocumentos().isEmpty()) {
            throw new IllegalArgumentException("No se puede crear un vehículo sin que tenga un documento asociado.");
        }

        // Validación de placa según tipo de vehículo
        validarPlacaPorTipo(vehiculo.getPlaca(), vehiculo.getTipoVehiculo());
        vehiculo.setPlaca(vehiculo.getPlaca().trim().toUpperCase());

        // Regla: La placa debe ser única
        if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
            throw new IllegalArgumentException("Ya existe un vehículo registrado con la placa: " + vehiculo.getPlaca());
        }

        // Regla: El estado inicial de los documentos asociados al crear debe ser 'En Verificación'
        for (VehiculoDocumento doc : vehiculo.getDocumentos()) {
            doc.setVehiculo(vehiculo);
            doc.setEstado("En Verificación");

            if (doc.getDocumento() == null || doc.getDocumento().getId() <= 0) {
                throw new IllegalArgumentException("Cada documento asociado debe especificar un ID de documento válido.");
            }

            // Validar que el documento parametrizado exista en base de datos
            Documento docParam = documentoRepository.findById(doc.getDocumento().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Documento parametrizado no encontrado con el ID: " + doc.getDocumento().getId()));
            doc.setDocumento(docParam);

            // Validar fechas del documento
            if (doc.getFechaExpedicion() == null || doc.getFechaVencimiento() == null) {
                throw new IllegalArgumentException("Las fechas de expedición y vencimiento son obligatorias para cada documento.");
            }
            if (doc.getFechaVencimiento().isBefore(doc.getFechaExpedicion())) {
                throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de expedición.");
            }
        }

        return vehiculoRepository.save(vehiculo);
    }

    // Actualizar vehículo existente (PUT)
    public Optional<Vehiculo> actualizar(int id, Vehiculo vehiculoDetalles) {
        return vehiculoRepository.findById(id).map(vehiculo -> {
            // Si la placa cambió, validar formato y unicidad
            String nuevaPlaca = vehiculoDetalles.getPlaca() != null ? vehiculoDetalles.getPlaca().trim().toUpperCase() : null;
            if (nuevaPlaca != null && !nuevaPlaca.equalsIgnoreCase(vehiculo.getPlaca())) {
                validarPlacaPorTipo(nuevaPlaca, vehiculoDetalles.getTipoVehiculo() != null ? vehiculoDetalles.getTipoVehiculo() : vehiculo.getTipoVehiculo());
                if (vehiculoRepository.existsByPlaca(nuevaPlaca)) {
                    throw new IllegalArgumentException("Ya existe un vehículo con la placa: " + nuevaPlaca);
                }
                vehiculo.setPlaca(nuevaPlaca);
            }

            if (vehiculoDetalles.getTipoVehiculo() != null) {
                vehiculo.setTipoVehiculo(vehiculoDetalles.getTipoVehiculo());
            }
            if (vehiculoDetalles.getTipoServicio() != null) {
                vehiculo.setTipoServicio(vehiculoDetalles.getTipoServicio());
            }
            if (vehiculoDetalles.getTipoCombustible() != null) {
                vehiculo.setTipoCombustible(vehiculoDetalles.getTipoCombustible());
            }
            if (vehiculoDetalles.getCapacidadPasajeros() != null) {
                vehiculo.setCapacidadPasajeros(vehiculoDetalles.getCapacidadPasajeros());
            }
            if (vehiculoDetalles.getColor() != null) {
                vehiculo.setColor(vehiculoDetalles.getColor());
            }
            if (vehiculoDetalles.getModelo() != null) {
                vehiculo.setModelo(vehiculoDetalles.getModelo());
            }
            if (vehiculoDetalles.getMarca() != null) {
                vehiculo.setMarca(vehiculoDetalles.getMarca());
            }
            if (vehiculoDetalles.getLinea() != null) {
                vehiculo.setLinea(vehiculoDetalles.getLinea());
            }

            return vehiculoRepository.save(vehiculo);
        });
    }

    // Eliminar vehículo por ID
    public void eliminar(int id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehículo no encontrado con el ID: " + id);
        }
        vehiculoRepository.deleteById(id);
    }

    // Agregar documento asociado a un vehículo existente
    public Optional<Vehiculo> agregarDocumentoAVehiculo(int vehiculoId, VehiculoDocumento nuevoDocumento) {
        return vehiculoRepository.findById(vehiculoId).map(vehiculo -> {
            nuevoDocumento.setVehiculo(vehiculo);

            // Validar que el documento parametrizado exista
            if (nuevoDocumento.getDocumento() == null || nuevoDocumento.getDocumento().getId() <= 0) {
                throw new IllegalArgumentException("Debe especificar un ID de documento parametrizado válido.");
            }
            Documento docParam = documentoRepository.findById(nuevoDocumento.getDocumento().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Documento parametrizado no encontrado con el ID: " + nuevoDocumento.getDocumento().getId()));
            nuevoDocumento.setDocumento(docParam);

            // Validar fechas
            if (nuevoDocumento.getFechaExpedicion() == null || nuevoDocumento.getFechaVencimiento() == null) {
                throw new IllegalArgumentException("Las fechas de expedición y vencimiento son obligatorias.");
            }
            if (nuevoDocumento.getFechaVencimiento().isBefore(nuevoDocumento.getFechaExpedicion())) {
                throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de expedición.");
            }

            // Asignar el estado por defecto o validar el estado proporcionado
            if (nuevoDocumento.getEstado() == null || nuevoDocumento.getEstado().trim().isEmpty()) {
                nuevoDocumento.setEstado("En Verificación");
            } else {
                String estado = nuevoDocumento.getEstado().trim();
                if (!estado.equals("Habilitado") && !estado.equals("Vencido") && !estado.equals("En Verificación")) {
                    throw new IllegalArgumentException("El estado del documento debe ser 'Habilitado', 'Vencido' o 'En Verificación'.");
                }
                nuevoDocumento.setEstado(estado);
            }

            vehiculo.getDocumentos().add(nuevoDocumento);
            return vehiculoRepository.save(vehiculo);
        });
    }
}