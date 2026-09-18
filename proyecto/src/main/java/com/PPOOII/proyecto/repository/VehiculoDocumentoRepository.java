package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.VehiculoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoDocumentoRepository extends JpaRepository<VehiculoDocumento, Integer> {
    List<VehiculoDocumento> findByVehiculoId(int vehiculoId);

@org.springframework.data.jpa.repository.Query("SELECT DISTINCT vd.vehiculo FROM VehiculoDocumento vd WHERE vd.fechaVencimiento < CURRENT_DATE")
    java.util.List<com.PPOOII.proyecto.entities.Vehiculo> findVehiculosConDocumentosVencidos();

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT vd.vehiculo FROM VehiculoDocumento vd WHERE vd.fechaVencimiento BETWEEN CURRENT_DATE AND :fechaLimite")
    java.util.List<com.PPOOII.proyecto.entities.Vehiculo> findVehiculosConDocumentosPorVencer(@org.springframework.data.repository.query.Param("fechaLimite") java.time.LocalDate fechaLimite);
}

