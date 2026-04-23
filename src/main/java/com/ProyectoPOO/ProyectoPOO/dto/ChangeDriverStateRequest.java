package com.ProyectoPOO.ProyectoPOO.dto;

import com.ProyectoPOO.ProyectoPOO.model.DriverVehicleState;
import lombok.Data;

@Data
public class ChangeDriverStateRequest {
    private DriverVehicleState state;
}

