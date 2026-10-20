// Paquete de repositorios de persistencia Spring Data JPA
package com.PPOOII.proyecto.repository;

// Importaciones de la entidad TrayectoTmp y utilidades de JPA
import com.PPOOII.proyecto.entities.TrayectoTmp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositorio para la entidad temporal trayecto_tmp y llamadas a Procedimientos Almacenados
@Repository
public interface TrayectoTmpRepository extends JpaRepository<TrayectoTmp, Long> {

    // Procedimiento 1: Inserción de registro individual en la tabla temporal con estado 'CARGADO'
    @Procedure(procedureName = "CARGAR_TRAYECTOS_EXCEL")
    void CARGAR_TRAYECTOS_EXCEL(
            @Param("p_idpersona_txt") String idpersonaTxt,
            @Param("p_idvehiculo_txt") String idvehiculoTxt,
            @Param("p_codigo_ruta") String codigoRuta,
            @Param("p_ubicacion") String ubicacion,
            @Param("p_orden_parada_txt") String ordenParadaTxt,
            @Param("p_latitud_txt") String latitudTxt,
            @Param("p_longitud_txt") String longitudTxt,
            @Param("p_login") String login,
            @Param("p_id_cargue") Long idCargue
    );

    // Procedimiento 2: Validación acumulativa de reglas de negocio para el lote de cargue
    @Procedure(procedureName = "VALIDAR_TRAYECTOS_CARGUE")
    void VALIDAR_TRAYECTOS_CARGUE(@Param("p_id_cargue") Long idCargue);

    // Procedimiento 3: Traslado de registros validados a la tabla transaccional definitiva 'trayecto'
    @Procedure(procedureName = "PROCESAR_TRAYECTOS_EXCEL")
    void PROCESAR_TRAYECTOS_EXCEL(@Param("p_id_cargue") Long idCargue);

    // Consulta de registros temporales según el identificador de cargue
    List<TrayectoTmp> findByIdCargueOrderByIdAsc(Long idCargue);

    // Conteo de registros por estado para un identificador de cargue específico
    long countByIdCargueAndEstado(Long idCargue, String estado);

    // Obtiene la lista de identificadores de cargue distintos ordenados del más reciente al más antiguo
    @Query("SELECT DISTINCT t.idCargue FROM TrayectoTmp t WHERE t.idCargue IS NOT NULL ORDER BY t.idCargue DESC")
    List<Long> findDistinctIdCargueDesc();
}
