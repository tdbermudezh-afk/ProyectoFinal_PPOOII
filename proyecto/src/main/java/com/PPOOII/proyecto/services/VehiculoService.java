package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    public List<Vehiculo> listarTodos() {
        return vehiculoRepository.findAll();
    }

    public Optional<Vehiculo> buscarPorId(int id) {
        return vehiculoRepository.findById(id);
    }

    public Optional<Vehiculo> buscarPorPlaca(String placa) {
        return vehiculoRepository.findByPlaca(placa);
    }

    public List<Vehiculo> buscarPorTipoVehiculo(String tipoVehiculo) {
        return vehiculoRepository.findByTipoVehiculo(tipoVehiculo);
    }

    public List<Vehiculo> buscarPorTipoDocumento(int documentoId) {
        return vehiculoRepository.findByTipoDocumento(documentoId);
    }

    public List<Vehiculo> buscarPorEstadoDocumento(String estado) {
        return vehiculoRepository.findByEstadoDocumento(estado);
    }

    public Vehiculo guardar(Vehiculo vehiculo) {
        if (vehiculo.getDocumentos() != null) {
            for (VehiculoDocumento doc : vehiculo.getDocumentos()) {
                doc.setVehiculo(vehiculo);
            }
        }
        return vehiculoRepository.save(vehiculo);
    }

    public Optional<Vehiculo> actualizar(int id, Vehiculo vehiculoDetalles) {
        return vehiculoRepository.findById(id).map(vehiculo -> {
            vehiculo.setPlaca(vehiculoDetalles.getPlaca());
            vehiculo.setTipoVehiculo(vehiculoDetalles.getTipoVehiculo());
            vehiculo.setTipoServicio(vehiculoDetalles.getTipoServicio());
            vehiculo.setTipoCombustible(vehiculoDetalles.getTipoCombustible());
            vehiculo.setCapacidadPasajeros(vehiculoDetalles.getCapacidadPasajeros());
            vehiculo.setColor(vehiculoDetalles.getColor());
            vehiculo.setModelo(vehiculoDetalles.getModelo());
            vehiculo.setMarca(vehiculoDetalles.getMarca());
            vehiculo.setLinea(vehiculoDetalles.getLinea());
            return vehiculoRepository.save(vehiculo);
        });
    }

    public void eliminar(int id) {
        vehiculoRepository.deleteById(id);
    }
    public Optional<Vehiculo> agregarDocumentoAVehiculo(int vehiculoId, VehiculoDocumento nuevoDocumento) {
    return vehiculoRepository.findById(vehiculoId).map(vehiculo -> {
        nuevoDocumento.setVehiculo(vehiculo);
        
        // Asignar el estado por defecto si no viene especificado
        if (nuevoDocumento.getEstado() == null || nuevoDocumento.getEstado().trim().isEmpty()) {
            nuevoDocumento.setEstado("En Verificación");
        }
        
        vehiculo.getDocumentos().add(nuevoDocumento);
        return vehiculoRepository.save(vehiculo);
    });
}
}