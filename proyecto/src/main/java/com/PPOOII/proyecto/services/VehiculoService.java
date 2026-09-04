package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoRepository vehiculoRepository;

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
        // Asignar la referencia del vehículo a cada documento asociado antes de guardar
        if (vehiculo.getDocumentos() != null) {
            for (VehiculoDocumento doc : vehiculo.getDocumentos()) {
                doc.setVehiculo(vehiculo);
            }
        }
        return vehiculoRepository.save(vehiculo);
    }

    public void eliminar(int id) {
        vehiculoRepository.deleteById(id);
    }
}