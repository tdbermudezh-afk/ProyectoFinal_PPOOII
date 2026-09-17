package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.VehiculoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoDocumentoRepository extends JpaRepository<VehiculoDocumento, Integer> {
    List<VehiculoDocumento> findByVehiculoId(int vehiculoId);
}