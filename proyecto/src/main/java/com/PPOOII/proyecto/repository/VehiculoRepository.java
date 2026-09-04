package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    // 1. Buscar vehículo por número de placa
    Optional<Vehiculo> findByPlaca(String placa);

    // 2. Buscar vehículos por tipo de vehículo (Automóvil / Motocicleta)
    List<Vehiculo> findByTipoVehiculo(String tipoVehiculo);

    // 3. Buscar vehículos que tengan en común un tipo de documento
    @Query("SELECT DISTINCT v FROM Vehiculo v JOIN v.documentos d WHERE d.documento.id = :documentoId")
    List<Vehiculo> findByTipoDocumento(@Param("documentoId") int documentoId);

    // 4. Buscar vehículos según el estado del documento (Habilitado, Vencido, En Verificación)
    @Query("SELECT DISTINCT v FROM Vehiculo v JOIN v.documentos d WHERE d.estado = :estado")
    List<Vehiculo> findByEstadoDocumento(@Param("estado") String estado);
}