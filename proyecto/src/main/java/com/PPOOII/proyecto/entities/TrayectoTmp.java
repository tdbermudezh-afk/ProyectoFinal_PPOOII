// Paquete de entidades del modelo de dominio de la aplicación
package com.PPOOII.proyecto.entities;

// Importaciones de Jakarta Persistence
import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.io.Serializable;

// Declaración de entidad mapeada a la tabla temporal 'trayecto_tmp'
@Entity
@Table(name = "trayecto_tmp")
@Check(constraints = "estado IN ('CARGADO', 'VALIDADO', 'PROCESADO', 'ERROR')")
public class TrayectoTmp implements Serializable {

    // Identificador único de serialización
    private static final long serialVersionUID = 1L;

    // Identificador autoincremental de la tabla temporal
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Conductor especificado en el Excel (puede ser identificación o ID en formato texto)
    @Column(name = "idpersona_txt", length = 100)
    private String idpersonaTxt;

    // Vehículo especificado en el Excel (puede ser placa o ID en formato texto)
    @Column(name = "idvehiculo_txt", length = 50)
    private String idvehiculoTxt;

    // Código identificador de la ruta que agrupa paradas
    @Column(name = "codigo_ruta", length = 50)
    private String codigoRuta;

    // Nombre descriptivo o dirección de la parada
    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    // Orden secuencial de la parada especificado en el archivo
    @Column(name = "orden_parada_txt", length = 10)
    private String ordenParadaTxt;

    // Coordenada de latitud en formato texto
    @Column(name = "latitud_txt", length = 50)
    private String latitudTxt;

    // Coordenada de longitud en formato texto
    @Column(name = "longitud_txt", length = 50)
    private String longitudTxt;

    // Login del usuario que realizó la operación de cargue
    @Column(name = "login", length = 100)
    private String login;

    // Identificador del lote de cargue en formato numérico yyyyMMddHHmm (ej. 202610170130)
    @Column(name = "id_cargue", nullable = false)
    private Long idCargue;

    // Estado del ciclo de cargue: CARGADO, VALIDADO, PROCESADO o ERROR
    @Column(name = "estado", nullable = false, length = 15)
    private String estado = "CARGADO";

    // Observaciones o inconsistencias acumulativas identificadas por el procedimiento de validación
    @Column(name = "observacion", length = 1000)
    private String observacion = "";

    // Constructor vacío por defecto requerido por JPA
    public TrayectoTmp() {}

    // Constructor parametrizado para facilitar inserciones
    public TrayectoTmp(String idpersonaTxt, String idvehiculoTxt, String codigoRuta, 
                       String ubicacion, String ordenParadaTxt, String latitudTxt, 
                       String longitudTxt, String login, Long idCargue) {
        this.idpersonaTxt = idpersonaTxt;
        this.idvehiculoTxt = idvehiculoTxt;
        this.codigoRuta = codigoRuta;
        this.ubicacion = ubicacion;
        this.ordenParadaTxt = ordenParadaTxt;
        this.latitudTxt = latitudTxt;
        this.longitudTxt = longitudTxt;
        this.login = login;
        this.idCargue = idCargue;
        this.estado = "CARGADO";
        this.observacion = "";
    }

    // Métodos Getter y Setter documentados
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIdpersonaTxt() { return idpersonaTxt; }
    public void setIdpersonaTxt(String idpersonaTxt) { this.idpersonaTxt = idpersonaTxt; }

    public String getIdvehiculoTxt() { return idvehiculoTxt; }
    public void setIdvehiculoTxt(String idvehiculoTxt) { this.idvehiculoTxt = idvehiculoTxt; }

    public String getCodigoRuta() { return codigoRuta; }
    public void setCodigoRuta(String codigoRuta) { this.codigoRuta = codigoRuta; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getOrdenParadaTxt() { return ordenParadaTxt; }
    public void setOrdenParadaTxt(String ordenParadaTxt) { this.ordenParadaTxt = ordenParadaTxt; }

    public String getLatitudTxt() { return latitudTxt; }
    public void setLatitudTxt(String latitudTxt) { this.latitudTxt = latitudTxt; }

    public String getLongitudTxt() { return longitudTxt; }
    public void setLongitudTxt(String longitudTxt) { this.longitudTxt = longitudTxt; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public Long getIdCargue() { return idCargue; }
    public void setIdCargue(Long idCargue) { this.idCargue = idCargue; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
