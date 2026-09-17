// Paquete que contiene la lógica de negocio y servicios
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

// Componente de servicio de Spring que encapsula todas las reglas de negocio de los vehículos
@Service
public class VehiculoService {

    // Inyección del repositorio para operaciones sobre la tabla 'vehiculo'
    private final VehiculoRepository vehiculoRepository;

    // Inyección del repositorio para validar la existencia de documentos de configuración
    private final DocumentoRepository documentoRepository;

    // Inyección de dependencias mediante el constructor de la clase
    public VehiculoService(VehiculoRepository vehiculoRepository, DocumentoRepository documentoRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.documentoRepository = documentoRepository;
    }

    // Consulta general: Recupera todos los vehículos almacenados en la base de datos
    public List<Vehiculo> listarTodos() {
        return vehiculoRepository.findAll();
    }

    // Consulta individual: Busca un vehículo por su clave primaria ID o lanza excepción 404 si no existe
    public Vehiculo buscarPorId(int id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con el ID: " + id));
    }

    // Búsqueda 1: Localiza un vehículo específico mediante su número de placa
    public Optional<Vehiculo> buscarPorPlaca(String placa) {
        return vehiculoRepository.findByPlaca(placa);
    }

    // Búsqueda 2: Obtiene la lista de vehículos filtrados por tipo (Automóvil o Motocicleta)
    public List<Vehiculo> buscarPorTipoVehiculo(String tipoVehiculo) {
        return vehiculoRepository.findByTipoVehiculo(tipoVehiculo);
    }

    // Búsqueda 3: Encuentra todos los vehículos que comparten un tipo de documento determinado
    public List<Vehiculo> buscarPorTipoDocumento(int documentoId) {
        return vehiculoRepository.findByTipoDocumento(documentoId);
    }

    // Búsqueda 4: Filtra los vehículos según el estado actual de su documento ('Habilitado', 'Vencido', 'En Verificación')
    public List<Vehiculo> buscarPorEstadoDocumento(String estado) {
        return vehiculoRepository.findByEstadoDocumento(estado);
    }

    // Validación interna: Comprueba el formato reglamentario de la placa según el tipo de vehículo
    private void validarPlacaPorTipo(String placa, String tipoVehiculo) {
        // Verificar que la placa tenga exactamente 6 caracteres
        if (placa == null || placa.trim().length() != 6) {
            throw new IllegalArgumentException("La placa debe tener exactamente 6 caracteres.");
        }
        String placaUpper = placa.trim().toUpperCase();

        // Para Automóvil: los primeros 3 caracteres deben ser letras y los 3 últimos numéricos (ej. ABC123)
        if ("Automóvil".equalsIgnoreCase(tipoVehiculo)) {
            if (!placaUpper.matches("^[A-Z]{3}[0-9]{3}$")) {
                throw new IllegalArgumentException("Para tipo Automóvil, la placa debe contener 3 letras seguidas de 3 números (ejemplo: ABC123).");
            }
        // Para Motocicleta: los primeros 3 caracteres deben ser letras, seguidos de 2 números y terminar en una letra (ej. ABC12D)
        } else if ("Motocicleta".equalsIgnoreCase(tipoVehiculo)) {
            if (!placaUpper.matches("^[A-Z]{3}[0-9]{2}[A-Z]$")) {
                throw new IllegalArgumentException("Para tipo Motocicleta, la placa debe contener 3 letras seguidas de 2 números y terminar en una letra (ejemplo: ABC12D).");
            }
        // Rechazo de cualquier tipo de vehículo que no sea Automóvil o Motocicleta
        } else {
            throw new IllegalArgumentException("El tipo de vehículo debe ser 'Automóvil' o 'Motocicleta'.");
        }
    }

    // Operación Crear (POST): Registra un nuevo vehículo garantizando las restricciones requeridas
    public Vehiculo guardar(Vehiculo vehiculo) {
        // Regla obligatoria: No se puede crear un vehículo sin que tenga al menos un documento asociado
        if (vehiculo.getDocumentos() == null || vehiculo.getDocumentos().isEmpty()) {
            throw new IllegalArgumentException("No se puede crear un vehículo sin que tenga un documento asociado.");
        }

        // Validación del formato de placa según el tipo de vehículo indicado
        validarPlacaPorTipo(vehiculo.getPlaca(), vehiculo.getTipoVehiculo());
        vehiculo.setPlaca(vehiculo.getPlaca().trim().toUpperCase());

        // Regla obligatoria: Comprobar que la placa sea única en el sistema
        if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
            throw new IllegalArgumentException("Ya existe un vehículo registrado con la placa: " + vehiculo.getPlaca());
        }

        // Regla obligatoria: Poner como estado inicial de los documentos asociados 'En Verificación' al crearlo
        for (VehiculoDocumento doc : vehiculo.getDocumentos()) {
            // Asocia la referencia bidireccional entre el vehículo y su documento
            doc.setVehiculo(vehiculo);
            // Establece el estado inicial reglamentario
            doc.setEstado("En Verificación");

            // Valida que se haya especificado el ID del documento parametrizado
            if (doc.getDocumento() == null || doc.getDocumento().getId() <= 0) {
                throw new IllegalArgumentException("Cada documento asociado debe especificar un ID de documento válido.");
            }

            // Comprueba que el documento exista en el catálogo de documentos parametrizados
            Documento docParam = documentoRepository.findById(doc.getDocumento().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Documento parametrizado no encontrado con el ID: " + doc.getDocumento().getId()));
            doc.setDocumento(docParam);

            // Valida la obligatoriedad y congruencia cronológica de las fechas de expedición y vencimiento
            if (doc.getFechaExpedicion() == null || doc.getFechaVencimiento() == null) {
                throw new IllegalArgumentException("Las fechas de expedición y vencimiento son obligatorias para cada documento.");
            }
            if (doc.getFechaVencimiento().isBefore(doc.getFechaExpedicion())) {
                throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de expedición.");
            }
        }

        // Persiste el vehículo junto con sus documentos asociados en cascada
        return vehiculoRepository.save(vehiculo);
    }

    // Operación Actualizar (PUT): Modifica los datos de un vehículo existente
    public Optional<Vehiculo> actualizar(int id, Vehiculo vehiculoDetalles) {
        return vehiculoRepository.findById(id).map(vehiculo -> {
            // Si la placa se modifica, valida el nuevo formato y que no esté duplicada
            String nuevaPlaca = vehiculoDetalles.getPlaca() != null ? vehiculoDetalles.getPlaca().trim().toUpperCase() : null;
            if (nuevaPlaca != null && !nuevaPlaca.equalsIgnoreCase(vehiculo.getPlaca())) {
                validarPlacaPorTipo(nuevaPlaca, vehiculoDetalles.getTipoVehiculo() != null ? vehiculoDetalles.getTipoVehiculo() : vehiculo.getTipoVehiculo());
                if (vehiculoRepository.existsByPlaca(nuevaPlaca)) {
                    throw new IllegalArgumentException("Ya existe un vehículo con la placa: " + nuevaPlaca);
                }
                vehiculo.setPlaca(nuevaPlaca);
            }

            // Actualización condicional de atributos del vehículo
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

            // Guarda los cambios aplicados al vehículo
            return vehiculoRepository.save(vehiculo);
        });
    }

    // Operación Eliminar (DELETE): Remueve un vehículo del sistema por su ID
    public void eliminar(int id) {
        // Verifica si el vehículo existe antes de intentar eliminarlo
        if (!vehiculoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehículo no encontrado con el ID: " + id);
        }
        vehiculoRepository.deleteById(id);
    }

    // Operación Complementaria: Permite agregar nuevos documentos a un vehículo existente
    public Optional<Vehiculo> agregarDocumentoAVehiculo(int vehiculoId, VehiculoDocumento nuevoDocumento) {
        return vehiculoRepository.findById(vehiculoId).map(vehiculo -> {
            // Vincula el nuevo documento con la entidad de vehículo recuperada
            nuevoDocumento.setVehiculo(vehiculo);

            // Valida la existencia del documento de configuración en el catálogo
            if (nuevoDocumento.getDocumento() == null || nuevoDocumento.getDocumento().getId() <= 0) {
                throw new IllegalArgumentException("Debe especificar un ID de documento parametrizado válido.");
            }
            Documento docParam = documentoRepository.findById(nuevoDocumento.getDocumento().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Documento parametrizado no encontrado con el ID: " + nuevoDocumento.getDocumento().getId()));
            nuevoDocumento.setDocumento(docParam);

            // Valida las fechas de expedición y vencimiento
            if (nuevoDocumento.getFechaExpedicion() == null || nuevoDocumento.getFechaVencimiento() == null) {
                throw new IllegalArgumentException("Las fechas de expedición y vencimiento son obligatorias.");
            }
            if (nuevoDocumento.getFechaVencimiento().isBefore(nuevoDocumento.getFechaExpedicion())) {
                throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de expedición.");
            }

            // Si no se define estado se asigna 'En Verificación'; si se define, se valida que sea admitido
            if (nuevoDocumento.getEstado() == null || nuevoDocumento.getEstado().trim().isEmpty()) {
                nuevoDocumento.setEstado("En Verificación");
            } else {
                String estado = nuevoDocumento.getEstado().trim();
                if (!estado.equals("Habilitado") && !estado.equals("Vencido") && !estado.equals("En Verificación")) {
                    throw new IllegalArgumentException("El estado del documento debe ser 'Habilitado', 'Vencido' o 'En Verificación'.");
                }
                nuevoDocumento.setEstado(estado);
            }

            // Agrega el documento a la lista y actualiza el vehículo
            vehiculo.getDocumentos().add(nuevoDocumento);
            return vehiculoRepository.save(vehiculo);
        });
    }
}