package com.ProyectoPOO.ProyectoPOO.dto;

import com.ProyectoPOO.ProyectoPOO.model.DriverVehicleState;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignVehicleRequest {
    private Long vehicleId;
    private LocalDate associationDate;
    private DriverVehicleState state;
}

