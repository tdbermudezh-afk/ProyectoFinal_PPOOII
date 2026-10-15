// Paquete de repositorios de persistencia Spring Data JPA
package com.PPOOII.proyecto.repository;

// Importación de la entidad Trayecto y utilidades de JPA
import com.PPOOII.proyecto.entities.Trayecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// Importaciones de utilidades de colecciones
import java.util.List;

// Marcador de componente de persistencia de Spring Boot
@Repository
public interface TrayectoRepository extends JpaRepository<Trayecto, Long> {

    // Consulta 1: Obtener todos los trayectos de una ruta ordenados secuencialmente por parada
    @Query("SELECT t FROM Trayecto t WHERE t.codigoRuta = :codigoRuta ORDER BY t.ordenParada ASC")
    List<Trayecto> findByCodigoRutaOrderByOrdenParadaAsc(@Param("codigoRuta") String codigoRuta);

    // Consulta 2: Visualizar únicamente los códigos de ruta agrupados para un conductor por su identificación
    @Query("SELECT DISTINCT t.codigoRuta FROM Trayecto t WHERE t.persona.identificacion = :identificacion")
    List<String> findCodigosRutaByPersonaIdentificacion(@Param("identificacion") String identificacion);

    // Consulta 3: Visualizar el código de ruta y conductor agrupados para un vehículo específico por placa
    @Query("SELECT DISTINCT t.codigoRuta, t.persona.nombres, t.persona.apellidos, t.persona.identificacion FROM Trayecto t WHERE t.vehiculo.placa = :placa")
    List<Object[]> findRutasYConductorByVehiculoPlaca(@Param("placa") String placa);

    // Consulta 4: Consultar trayectos pendientes por geocodificación (donde latitud o longitud son nulas o 0)
    @Query("SELECT t FROM Trayecto t WHERE t.latitud IS NULL OR t.longitud IS NULL OR (t.latitud = 0.0 AND t.longitud = 0.0)")
    List<Trayecto> findTrayectosSinCoordenadas();

    // Consulta 5: Contar cantidad de paradas de un código de ruta específico
    long countByCodigoRuta(String codigoRuta);

    // Consulta 6: Listar todos los códigos de ruta distintos registrados en el sistema
    @Query("SELECT DISTINCT t.codigoRuta FROM Trayecto t")
    List<String> findDistinctCodigosRuta();
}

