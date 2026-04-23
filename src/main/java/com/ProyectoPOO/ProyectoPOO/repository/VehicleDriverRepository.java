package com.ProyectoPOO.ProyectoPOO.repository;

import com.ProyectoPOO.ProyectoPOO.model.DriverVehicleState;
import com.ProyectoPOO.ProyectoPOO.model.VehicleDriver;
import com.ProyectoPOO.ProyectoPOO.model.VehicleDriverId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleDriverRepository extends JpaRepository<VehicleDriver, VehicleDriverId> {
    List<VehicleDriver> findByState(DriverVehicleState state);
    List<VehicleDriver> findByPersona_Id(Long personaId);
}


