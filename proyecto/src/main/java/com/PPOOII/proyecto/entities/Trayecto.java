// Paquete de entidades del modelo de dominio de la aplicación
package com.PPOOII.proyecto.entities;

// Importaciones de JPA y validaciones de Jakarta
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

// Declaración de entidad mapeada a la tabla 'trayecto' de MySQL
@Entity
// Definición explícita del nombre de la tabla en base de datos
@Table(name = "trayecto")
public class Trayecto implements Serializable {

    // Identificador de serialización para compatibilidad binaria
    private static final long serialVersionUID = 1L;

    // Identificador único autoincremental de la parada o trayecto
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Many-to-One con la entidad Persona (debe ser conductor con rol 'C')
    @NotNull(message = "El conductor es obligatorio para el trayecto")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idpersona", nullable = false)
    private Persona persona;

    // Relación Many-to-One con la entidad Vehiculo que realiza el recorrido
    @NotNull(message = "El vehículo es obligatorio para el trayecto")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idvehiculo", nullable = false)
    private Vehiculo vehiculo;

    // Código identificador de la ruta que agrupa múltiples trayectos/paradas
    @NotBlank(message = "El código de ruta no puede estar vacío")
    @Column(name = "codigo_ruta", nullable = false, length = 50)
    private String codigoRuta;

    // Descripción textual del punto geográfico o parada (ej. 'Conservatorio del Tolima, Ibagué')
    @NotBlank(message = "La ubicación no puede estar vacía")
    @Column(name = "ubicacion", nullable = false, length = 255)
    private String ubicacion;

    // Orden secuencial de la parada: 0 es la inicial, el mayor es la final, intermedios son paradas
    @NotNull(message = "El orden de la parada es obligatorio")
    @Min(value = 0, message = "El orden de la parada no puede ser negativo")
    @Column(name = "orden_parada", nullable = false)
    private Integer ordenParada;

    // Coordenada geográfica de latitud calculada por geocodificación
    @Column(name = "latitud")
    private Double latitud;

    // Coordenada geográfica de longitud calculada por geocodificación
    @Column(name = "longitud")
    private Double longitud;

    // Login del usuario que registra el trayecto para auditoría y trazabilidad
    @NotBlank(message = "El login del usuario registrador es obligatorio")
    @Column(name = "login", nullable = false, length = 100)
    private String login;

    // Constructor vacío por defecto requerido por JPA/Hibernate
    public Trayecto() {}

    // Constructor completo parametrizado para instanciación ágil
    public Trayecto(Long id, Persona persona, Vehiculo vehiculo, String codigoRuta, 
                    String ubicacion, Integer ordenParada, Double latitud, Double longitud, String login) {
        this.id = id;
        this.persona = persona;
        this.vehiculo = vehiculo;
        this.codigoRuta = codigoRuta;
        this.ubicacion = ubicacion;
        this.ordenParada = ordenParada;
        this.latitud = latitud;
        this.longitud = longitud;
        this.login = login;
    }

    // Métodos Getter y Setter con documentación de cada atributo
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }

    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }

    public String getCodigoRuta() { return codigoRuta; }
    public void setCodigoRuta(String codigoRuta) { this.codigoRuta = codigoRuta; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public Integer getOrdenParada() { return ordenParada; }
    public void setOrdenParada(Integer ordenParada) { this.ordenParada = ordenParada; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
}

