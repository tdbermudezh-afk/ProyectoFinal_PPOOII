// Paquete de acceso a datos (Repositorios JPA)
package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Interfaz de repositorio Spring Data JPA para la entidad Vehiculo
// Proporciona automáticamente operaciones CRUD y consultas personalizadas
@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    // 1. Consulta derivada: Busca un vehículo a partir de su número de placa exacto
    Optional<Vehiculo> findByPlaca(String placa);

    // Consulta de existencia: Comprueba si ya existe un vehículo registrado con la placa indicada
    boolean existsByPlaca(String placa);

    // 2. Consulta derivada: Busca todos los vehículos pertenecientes a un tipo (Automóvil o Motocicleta)
    List<Vehiculo> findByTipoVehiculo(String tipoVehiculo);

    // 3. Consulta JPQL personalizada: Busca los vehículos que tienen asociado un tipo de documento específico por su ID
    @Query("SELECT DISTINCT v FROM Vehiculo v JOIN v.documentos d WHERE d.documento.id = :documentoId")
    List<Vehiculo> findByTipoDocumento(@Param("documentoId") int documentoId);

    // 4. Consulta JPQL personalizada: Busca los vehículos según el estado de sus documentos asociados ('Habilitado', 'Vencido', 'En Verificación')
    @Query("SELECT DISTINCT v FROM Vehiculo v JOIN v.documentos d WHERE d.estado = :estado")
    List<Vehiculo> findByEstadoDocumento(@Param("estado") String estado);
}