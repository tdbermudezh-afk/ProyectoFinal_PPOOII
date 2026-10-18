package com.PPOOII.proyecto.entities; // Define el paquete de entidades del proyecto

import jakarta.persistence.*; // Importa las anotaciones de persistencia JPA
import jakarta.validation.constraints.*; // Importa las anotaciones de validación de datos
import java.io.Serializable; // Importa la interfaz Serializable según el Laboratorio 1

@Entity // Marca la clase como una entidad persistente de JPA
@Table(name = "personas") // Asocia la entidad con la tabla personas de la base de datos
public class Persona implements Serializable { // Implementa Serializable según el estándar del Laboratorio 1

    private static final long serialVersionUID = 1L; // Identificador de versión de serialización

    @Id // Define la llave primaria de la entidad
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura el autoincremento en la base de datos
    private Long id; // Identificador único de la persona

    @NotBlank(message = "La identificación es obligatoria") // Valida que la identificación no esté vacía
    @Column(nullable = false, unique = true) // Columna obligatoria y con restricción de unicidad
    private String identificacion; // Número de documento de identidad

    @NotBlank(message = "El tipo de identificación es obligatorio") // Valida presencia del tipo de documento
    @Pattern(regexp = "^(CC|CE|PASAPORTE)$", message = "Tipo de identificación inválido (Permitidos: CC, CE, PASAPORTE)") // Restricción de dominio
    @Column(name = "tipo_identificacion", nullable = false) // Mapea la columna tipo_identificacion
    private String tipoIdentificacion; // Tipo de identificación (CC, CE o PASAPORTE)

    @NotBlank(message = "El nombre es obligatorio") // Valida que el nombre no sea vacío
    @Column(nullable = false) // Columna no nula
    private String nombres; // Nombres de la persona

    @NotBlank(message = "El apellido es obligatorio") // Valida que el apellido no sea vacío
    @Column(nullable = false) // Columna no nula
    private String apellidos; // Apellidos de la persona

    @Email(message = "Formato de correo electrónico inválido") // Valida formato de email
    @NotBlank(message = "El correo es obligatorio") // Valida que el correo no esté vacío
    @Column(nullable = false, unique = true) // Columna obligatoria y única
    private String correo; // Correo electrónico de la persona

    @NotBlank(message = "El tipo de persona es obligatorio") // Valida presencia del rol
    @Pattern(regexp = "^[CA]$", message = "Tipo de persona inválido (C - Conductor, A - Administrativo)") // Restricción C o A
    @Column(name = "tipo_persona", nullable = false, length = 1) // Columna de 1 carácter
    private String tipoPersona; // Tipo de persona: 'C' (Conductor) o 'A' (Administrativo)

    // Campo temporal para retornar el usuario generado automáticamente al crear un administrativo
    @Transient // No se persiste como columna en la tabla personas
    private Usuario usuarioGenerado; // Referencia al usuario creado automáticamente

    // ENTREGA 3: Documento de licencia de conducción en formato Base64 (BLOB/LONGTEXT)
    @Lob // Indica almacenamiento de objeto largo
    @Column(name = "licencia_conduccion", columnDefinition = "LONGTEXT") // Columna LONGTEXT en MySQL
    private String licenciaConduccion; // Cadena en formato Base64 del PDF de la licencia

    // ENTREGA 3: Fecha límite de vigencia de la licencia de conducción
    @Column(name = "fecha_vigencia_licencia") // Columna DATE para la vigencia de la licencia
    private java.time.LocalDate fechaVigenciaLicencia; // Fecha de vencimiento de la licencia

    // Constructor vacío por defecto requerido por JPA
    public Persona() {} // Constructor sin argumentos

    // Métodos Getter y Setter documentados
    public Long getId() { return id; } // Obtiene el ID de la persona
    public void setId(Long id) { this.id = id; } // Establece el ID de la persona

    public String getIdentificacion() { return identificacion; } // Obtiene la identificación
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; } // Establece la identificación

    public String getTipoIdentificacion() { return tipoIdentificacion; } // Obtiene el tipo de identificación
    public void setTipoIdentificacion(String tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; } // Establece el tipo de identificación

    public String getNombres() { return nombres; } // Obtiene los nombres
    public void setNombres(String nombres) { this.nombres = nombres; } // Establece los nombres

    public String getApellidos() { return apellidos; } // Obtiene los apellidos
    public void setApellidos(String apellidos) { this.apellidos = apellidos; } // Establece los apellidos

    public String getCorreo() { return correo; } // Obtiene el correo electrónico
    public void setCorreo(String correo) { this.correo = correo; } // Establece el correo electrónico

    public String getTipoPersona() { return tipoPersona; } // Obtiene el tipo de persona (C o A)
    public void setTipoPersona(String tipoPersona) { this.tipoPersona = tipoPersona; } // Establece el tipo de persona

    public Usuario getUsuarioGenerado() { return usuarioGenerado; } // Obtiene el usuario autogenerado
    public void setUsuarioGenerado(Usuario usuarioGenerado) { this.usuarioGenerado = usuarioGenerado; } // Establece el usuario autogenerado

    public String getLicenciaConduccion() { return licenciaConduccion; } // Obtiene la licencia en Base64
    public void setLicenciaConduccion(String licenciaConduccion) { this.licenciaConduccion = licenciaConduccion; } // Establece la licencia en Base64

    public java.time.LocalDate getFechaVigenciaLicencia() { return fechaVigenciaLicencia; } // Obtiene la fecha de vigencia de licencia
    public void setFechaVigenciaLicencia(java.time.LocalDate fechaVigenciaLicencia) { this.fechaVigenciaLicencia = fechaVigenciaLicencia; } // Establece la fecha de vigencia de licencia

    @Override // Sobrescribe el método toString según el Laboratorio 1
    public String toString() { // Representación textual del objeto Persona
        return "Persona [id=" + this.id + ", identificacion=" + this.identificacion + ", nombres=" + this.nombres + ", apellidos=" + this.apellidos + ", tipoPersona=" + this.tipoPersona + "]"; // Retorna cadena descriptiva
    } // Fin del método toString
} // Fin de la clase Persona