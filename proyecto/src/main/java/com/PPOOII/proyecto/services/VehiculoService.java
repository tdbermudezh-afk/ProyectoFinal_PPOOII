// Paquete que contiene la lógica de negocio y servicios de la aplicación
package com.PPOOII.proyecto.services;

// Importación de la entidad paramétrica Documento
import com.PPOOII.proyecto.entities.Documento;
// Importación de la entidad principal Vehiculo
import com.PPOOII.proyecto.entities.Vehiculo;
// Importación de la entidad de relación VehiculoDocumento
import com.PPOOII.proyecto.entities.VehiculoDocumento;
// Importación de la excepción personalizada para recursos no encontrados (HTTP 404)
import com.PPOOII.proyecto.exceptions.ResourceNotFoundException;
// Importación del repositorio JPA de Documento
import com.PPOOII.proyecto.repository.DocumentoRepository;
// Importación del repositorio JPA de Vehiculo
import com.PPOOII.proyecto.repository.VehiculoRepository;
// Importación de la interfaz de servicio IVehiculoService (Laboratorio 1)
import com.PPOOII.proyecto.services.Interfaces.IVehiculoService;
// Importación del gestor de logs Log4j2 (Laboratorio 1)
import org.apache.logging.log4j.LogManager;
// Importación de la interfaz Logger de Log4j2 (Laboratorio 1)
import org.apache.logging.log4j.Logger;
// Importación de la anotación @Service de Spring
import org.springframework.stereotype.Service;

// Importación de la estructura List de Java
import java.util.List;
// Importación del contenedor Optional de Java
import java.util.Optional;

// Componente de servicio de Spring que implementa IVehiculoService y encapsula las reglas de negocio de vehículos
@Service("VehiculoService")
public class VehiculoService implements IVehiculoService {

    // Instancia del logger de auditoría según el estándar del Laboratorio 1
    private static final Logger logger = LogManager.getLogger(VehiculoService.class);

    // Inyección del repositorio para operaciones sobre la tabla 'vehiculo'
    private final VehiculoRepository vehiculoRepository;

    // Inyección del repositorio para validar la existencia de documentos de configuración
    private final DocumentoRepository documentoRepository;

    // Inyección de dependencias mediante el constructor de la clase
    public VehiculoService(VehiculoRepository vehiculoRepository, DocumentoRepository documentoRepository) {
        // Asigna la instancia del repositorio de vehículos
        this.vehiculoRepository = vehiculoRepository;
        // Asigna la instancia del repositorio de documentos
        this.documentoRepository = documentoRepository;
    }

    // Consulta general: Recupera todos los vehículos almacenados en la base de datos
    @Override
    public List<Vehiculo> listarTodos() {
        // Registra en el log la consulta general de vehículos
        logger.info("Consultando el listado completo de vehículos registrados.");
        // Retorna todos los vehículos desde el repositorio JPA
        return vehiculoRepository.findAll();
    }

    // Consulta individual: Busca un vehículo por su clave primaria ID o lanza excepción 404 si no existe
    @Override
    public Vehiculo buscarPorId(int id) {
        // Registra en el log la búsqueda por ID
        logger.info("Buscando vehículo con ID: {}", id);
        // Retorna el vehículo encontrado o lanza ResourceNotFoundException
        return vehiculoRepository.findById(id)
                // Si no existe el registro, lanza la excepción con código 404
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con el ID: " + id));
    }

    // Búsqueda 1: Localiza un vehículo específico mediante su número de placa
    @Override
    public Optional<Vehiculo> buscarPorPlaca(String placa) {
        // Registra en el log la búsqueda por placa
        logger.info("Buscando vehículo por placa: {}", placa);
        // Ejecuta la consulta en el repositorio por placa
        return vehiculoRepository.findByPlaca(placa);
    }

    // Búsqueda 2: Obtiene la lista de vehículos filtrados por tipo (Automóvil o Motocicleta)
    @Override
    public List<Vehiculo> buscarPorTipoVehiculo(String tipoVehiculo) {
        // Registra en el log la búsqueda por tipo de vehículo
        logger.info("Buscando vehículos por tipo: {}", tipoVehiculo);
        // Ejecuta la consulta en el repositorio por tipo de vehículo
        return vehiculoRepository.findByTipoVehiculo(tipoVehiculo);
    }

    // Búsqueda 3: Encuentra todos los vehículos que comparten un tipo de documento determinado
    @Override
    public List<Vehiculo> buscarPorTipoDocumento(int documentoId) {
        // Registra en el log la búsqueda por tipo de documento
        logger.info("Buscando vehículos asociados al tipo de documento ID: {}", documentoId);
        // Ejecuta la consulta en el repositorio por ID de documento
        return vehiculoRepository.findByTipoDocumento(documentoId);
    }

    // Búsqueda 4: Filtra los vehículos según el estado actual de su documento ('Habilitado', 'Vencido', 'En Verificación')
    @Override
    public List<Vehiculo> buscarPorEstadoDocumento(String estado) {
        // Registra en el log la búsqueda por estado del documento
        logger.info("Buscando vehículos con documentos en estado: {}", estado);
        // Ejecuta la consulta en el repositorio por estado de documento
        return vehiculoRepository.findByEstadoDocumento(estado);
    }

    // Validación interna: Comprueba el formato reglamentario de la placa según el tipo de vehículo
    private void validarPlacaPorTipo(String placa, String tipoVehiculo) {
        // Verificar que la placa tenga exactamente 6 caracteres
        if (placa == null || placa.trim().length() != 6) {
            // Lanza excepción cuando la longitud de la placa es distinta de 6
            throw new IllegalArgumentException("La placa debe tener exactamente 6 caracteres.");
        }
        // Normaliza la placa eliminando espacios y convirtiendo a mayúsculas
        String placaUpper = placa.trim().toUpperCase();

        // Para Automóvil: los primeros 3 caracteres deben ser letras y los 3 últimos numéricos (ej. ABC123)
        if ("Automóvil".equalsIgnoreCase(tipoVehiculo)) {
            // Verifica que cumpla la expresión regular de automóvil
            if (!placaUpper.matches("^[A-Z]{3}[0-9]{3}$")) {
                // Lanza excepción si el formato de placa no corresponde a un automóvil
                throw new IllegalArgumentException("Para tipo Automóvil, la placa debe contener 3 letras seguidas de 3 números (ejemplo: ABC123).");
            }
        // Para Motocicleta: los primeros 3 caracteres deben ser letras, seguidos de 2 números y terminar en una letra (ej. ABC12D)
        } else if ("Motocicleta".equalsIgnoreCase(tipoVehiculo)) {
            // Verifica que cumpla la expresión regular de motocicleta
            if (!placaUpper.matches("^[A-Z]{3}[0-9]{2}[A-Z]$")) {
                // Lanza excepción si el formato de placa no corresponde a una motocicleta
                throw new IllegalArgumentException("Para tipo Motocicleta, la placa debe contener 3 letras seguidas de 2 números y terminar en una letra (ejemplo: ABC12D).");
            }
        // Rechazo de cualquier tipo de vehículo que no sea Automóvil o Motocicleta
        } else {
            // Lanza excepción cuando el tipo de vehículo no es válido
            throw new IllegalArgumentException("El tipo de vehículo debe ser 'Automóvil' o 'Motocicleta'.");
        }
    }

    // Validación de compatibilidad entre el documento y el tipo de vehículo
    private void validarCompatibilidadDocumento(String tipoVehiculo, Documento docParam) {
        // Si alguno de los parámetros es nulo, omite la validación adicional
        if (tipoVehiculo == null || docParam == null || docParam.getAplicaA() == null) return;
        // Normaliza el código de aplicabilidad del documento ('A', 'M' o 'AM')
        String aplica = docParam.getAplicaA().trim().toUpperCase();
        // Verifica incompatibilidad entre Automóvil y documento exclusivo de Motocicleta
        if ("Automóvil".equalsIgnoreCase(tipoVehiculo) && "M".equals(aplica)) {
            // Lanza excepción indicando que el documento solo aplica a motocicletas
            throw new IllegalArgumentException("El documento '" + docParam.getNombre() + "' solo aplica para Motocicletas.");
        }
        // Verifica incompatibilidad entre Motocicleta y documento exclusivo de Automóvil
        if ("Motocicleta".equalsIgnoreCase(tipoVehiculo) && "A".equals(aplica)) {
            // Lanza excepción indicando que el documento solo aplica a automóviles
            throw new IllegalArgumentException("El documento '" + docParam.getNombre() + "' solo aplica para Automóviles.");
        }
    }

    // Operación Crear (POST): Registra un nuevo vehículo garantizando las restricciones requeridas
    @Override
    public Vehiculo guardar(Vehiculo vehiculo) {
        // Regla obligatoria: No se puede crear un vehículo sin que tenga al menos un documento asociado
        if (vehiculo.getDocumentos() == null || vehiculo.getDocumentos().isEmpty()) {
            // Lanza excepción cuando la colección de documentos está vacía o nula
            throw new IllegalArgumentException("No se puede crear un vehículo sin que tenga un documento asociado.");
        }

        // Validación del formato de placa según el tipo de vehículo indicado
        validarPlacaPorTipo(vehiculo.getPlaca(), vehiculo.getTipoVehiculo());
        // Asigna la placa normalizada en mayúsculas
        vehiculo.setPlaca(vehiculo.getPlaca().trim().toUpperCase());

        // Regla obligatoria: Comprobar que la placa sea única en el sistema
        if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
            // Lanza excepción si la placa ya se encuentra registrada
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
                // Lanza excepción si no se indicó un ID de documento válido
                throw new IllegalArgumentException("Cada documento asociado debe especificar un ID de documento válido.");
            }

            // Comprueba que el documento exista en el catálogo de documentos parametrizados
            Documento docParam = documentoRepository.findById(doc.getDocumento().getId())
                    // Lanza ResourceNotFoundException si el documento parametrizado no existe
                    .orElseThrow(() -> new ResourceNotFoundException("Documento parametrizado no encontrado con el ID: " + doc.getDocumento().getId()));
            // Asigna la entidad de documento parametrizado recuperada
            doc.setDocumento(docParam);

            // Valida compatibilidad entre el documento y el tipo de vehículo
            validarCompatibilidadDocumento(vehiculo.getTipoVehiculo(), docParam);

            // Valida la obligatoriedad y congruencia cronológica de las fechas de expedición y vencimiento
            if (doc.getFechaExpedicion() == null || doc.getFechaVencimiento() == null) {
                // Lanza excepción si falta alguna de las fechas
                throw new IllegalArgumentException("Las fechas de expedición y vencimiento son obligatorias para cada documento.");
            }
            // Verifica que la fecha de vencimiento no sea anterior a la fecha de expedición
            if (doc.getFechaVencimiento().isBefore(doc.getFechaExpedicion())) {
                // Lanza excepción por incongruencia cronológica de fechas
                throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de expedición.");
            }
        }

        // Registra en el log la creación del nuevo vehículo
        logger.info("Guardando nuevo vehículo con placa: {}", vehiculo.getPlaca());
        // Persiste el vehículo junto con sus documentos asociados en cascada
        return vehiculoRepository.save(vehiculo);
    }

    // Operación Actualizar (PUT): Modifica los datos de un vehículo existente
    @Override
    public Optional<Vehiculo> actualizar(int id, Vehiculo vehiculoDetalles) {
        // Busca el vehículo por su ID y aplica las modificaciones si existe
        return vehiculoRepository.findById(id).map(vehiculo -> {
            // Si la placa se modifica, valida el nuevo formato y que no esté duplicada
            String nuevaPlaca = vehiculoDetalles.getPlaca() != null ? vehiculoDetalles.getPlaca().trim().toUpperCase() : null;
            // Verifica si se proporcionó una placa distinta a la actual
            if (nuevaPlaca != null && !nuevaPlaca.equalsIgnoreCase(vehiculo.getPlaca())) {
                // Valida el formato de la nueva placa según el tipo de vehículo
                validarPlacaPorTipo(nuevaPlaca, vehiculoDetalles.getTipoVehiculo() != null ? vehiculoDetalles.getTipoVehiculo() : vehiculo.getTipoVehiculo());
                // Verifica que la nueva placa no pertenezca ya a otro vehículo
                if (vehiculoRepository.existsByPlaca(nuevaPlaca)) {
                    // Lanza excepción por placa duplicada
                    throw new IllegalArgumentException("Ya existe un vehículo con la placa: " + nuevaPlaca);
                }
                // Actualiza la placa del vehículo
                vehiculo.setPlaca(nuevaPlaca);
            }

            // Actualización condicional del tipo de vehículo validando compatibilidad con la placa
            if (vehiculoDetalles.getTipoVehiculo() != null) {
                // Obtiene la placa efectiva actual del vehículo
                String placaEfectiva = vehiculo.getPlaca();
                // Comprueba que la placa sea válida para el nuevo tipo de vehículo
                validarPlacaPorTipo(placaEfectiva, vehiculoDetalles.getTipoVehiculo());
                // Asigna el nuevo tipo de vehículo
                vehiculo.setTipoVehiculo(vehiculoDetalles.getTipoVehiculo());
            }
            // Actualiza el tipo de servicio si fue enviado en la petición
            if (vehiculoDetalles.getTipoServicio() != null) {
                // Asigna el nuevo tipo de servicio
                vehiculo.setTipoServicio(vehiculoDetalles.getTipoServicio());
            }
            // Actualiza el tipo de combustible si fue enviado en la petición
            if (vehiculoDetalles.getTipoCombustible() != null) {
                // Asigna el nuevo tipo de combustible
                vehiculo.setTipoCombustible(vehiculoDetalles.getTipoCombustible());
            }
            // Actualiza la capacidad de pasajeros si fue enviada en la petición
            if (vehiculoDetalles.getCapacidadPasajeros() != null) {
                // Asigna la nueva capacidad de pasajeros
                vehiculo.setCapacidadPasajeros(vehiculoDetalles.getCapacidadPasajeros());
            }
            // Actualiza el color si fue enviado en la petición
            if (vehiculoDetalles.getColor() != null) {
                // Asigna el nuevo color
                vehiculo.setColor(vehiculoDetalles.getColor());
            }
            // Actualiza el modelo si fue enviado en la petición
            if (vehiculoDetalles.getModelo() != null) {
                // Asigna el nuevo año de modelo
                vehiculo.setModelo(vehiculoDetalles.getModelo());
            }
            // Actualiza la marca si fue enviada en la petición
            if (vehiculoDetalles.getMarca() != null) {
                // Asigna la nueva marca
                vehiculo.setMarca(vehiculoDetalles.getMarca());
            }
            // Actualiza la línea si fue enviada en la petición
            if (vehiculoDetalles.getLinea() != null) {
                // Asigna la nueva línea
                vehiculo.setLinea(vehiculoDetalles.getLinea());
            }

            // Registra en el log la actualización del vehículo
            logger.info("Actualizando vehículo con ID: {}", id);
            // Guarda los cambios aplicados al vehículo
            return vehiculoRepository.save(vehiculo);
        });
    }

    // Operación Eliminar (DELETE): Remueve un vehículo del sistema por su ID
    @Override
    public void eliminar(int id) {
        // Verifica si el vehículo existe antes de intentar eliminarlo
        if (!vehiculoRepository.existsById(id)) {
            // Lanza excepción 404 si el vehículo no existe
            throw new ResourceNotFoundException("Vehículo no encontrado con el ID: " + id);
        }
        // Registra en el log la eliminación del vehículo
        logger.info("Eliminando vehículo con ID: {}", id);
        // Ejecuta el borrado por ID en el repositorio
        vehiculoRepository.deleteById(id);
    }

    // Operación Complementaria: Permite agregar nuevos documentos a un vehículo existente
    @Override
    public Optional<Vehiculo> agregarDocumentoAVehiculo(int vehiculoId, VehiculoDocumento nuevoDocumento) {
        // Busca el vehículo por su ID y le asocia el nuevo documento
        return vehiculoRepository.findById(vehiculoId).map(vehiculo -> {
            // Vincula el nuevo documento con la entidad de vehículo recuperada
            nuevoDocumento.setVehiculo(vehiculo);

            // Valida la existencia del documento de configuración en el catálogo
            if (nuevoDocumento.getDocumento() == null || nuevoDocumento.getDocumento().getId() <= 0) {
                // Lanza excepción si no se especificó un ID de documento válido
                throw new IllegalArgumentException("Debe especificar un ID de documento parametrizado válido.");
            }
            // Busca el documento parametrizado en la base de datos
            Documento docParam = documentoRepository.findById(nuevoDocumento.getDocumento().getId())
                    // Lanza excepción 404 si el documento parametrizado no existe
                    .orElseThrow(() -> new ResourceNotFoundException("Documento parametrizado no encontrado con el ID: " + nuevoDocumento.getDocumento().getId()));
            // Valida que el documento aplique al tipo de vehículo
            validarCompatibilidadDocumento(vehiculo.getTipoVehiculo(), docParam);
            // Asigna la entidad de documento parametrizado
            nuevoDocumento.setDocumento(docParam);

            // Valida las fechas de expedición y vencimiento
            if (nuevoDocumento.getFechaExpedicion() == null || nuevoDocumento.getFechaVencimiento() == null) {
                // Lanza excepción si falta alguna fecha
                throw new IllegalArgumentException("Las fechas de expedición y vencimiento son obligatorias.");
            }
            // Comprueba que la fecha de vencimiento no sea anterior a la de expedición
            if (nuevoDocumento.getFechaVencimiento().isBefore(nuevoDocumento.getFechaExpedicion())) {
                // Lanza excepción si la fecha de vencimiento es anterior a la de expedición
                throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de expedición.");
            }

            // Si no se define estado se asigna 'En Verificación'; si se define, se valida que sea admitido
            if (nuevoDocumento.getEstado() == null || nuevoDocumento.getEstado().trim().isEmpty()) {
                // Asigna el estado por defecto 'En Verificación'
                nuevoDocumento.setEstado("En Verificación");
            } else {
                // Obtiene el estado limpio de espacios laterales
                String estado = nuevoDocumento.getEstado().trim();
                // Comprueba que el estado coincida con uno de los valores permitidos
                if (!estado.equals("Habilitado") && !estado.equals("Vencido") && !estado.equals("En Verificación")) {
                    // Lanza excepción si el estado no es válido
                    throw new IllegalArgumentException("El estado del documento debe ser 'Habilitado', 'Vencido' o 'En Verificación'.");
                }
                // Asigna el estado validado
                nuevoDocumento.setEstado(estado);
            }

            // Registra en el log la adición del documento al vehículo
            logger.info("Agregando documento '{}' al vehículo ID: {}", docParam.getNombre(), vehiculoId);
            // Agrega el documento a la lista y actualiza el vehículo
            vehiculo.getDocumentos().add(nuevoDocumento);
            // Guarda y retorna el vehículo actualizado
            return vehiculoRepository.save(vehiculo);
        });
    }
}