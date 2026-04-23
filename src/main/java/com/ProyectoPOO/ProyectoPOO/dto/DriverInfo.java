package com.ProyectoPOO.ProyectoPOO.dto;

import com.ProyectoPOO.ProyectoPOO.model.DriverVehicleState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverInfo {
    private Long personId;
    private String identification;
    private String names;
    private String lastNames;
    private DriverVehicleState state;
    private LocalDate associationDate;
}

