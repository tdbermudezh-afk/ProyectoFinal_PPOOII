package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VehiculoDocumentoRepository extends JpaRepository<VehiculoDocumento, Integer> {
    @Query("SELECT vd FROM VehiculoDocumento vd WHERE vd.vehiculo.id = :vehiculoId")
    List<VehiculoDocumento> findByVehiculoId(@Param("vehiculoId") int vehiculoId);

    @Query("SELECT DISTINCT vd.vehiculo FROM VehiculoDocumento vd WHERE vd.fechaVencimiento < CURRENT_DATE OR vd.estado = 'Vencido'")
    List<Vehiculo> findVehiculosConDocumentosVencidos();

    @Query("SELECT DISTINCT vd.vehiculo FROM VehiculoDocumento vd WHERE vd.fechaVencimiento BETWEEN CURRENT_DATE AND :fechaLimite")
    List<Vehiculo> findVehiculosConDocumentosPorVencer(@Param("fechaLimite") LocalDate fechaLimite);
}
