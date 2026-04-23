package com.ProyectoPOO.ProyectoPOO.dto;

import com.ProyectoPOO.ProyectoPOO.model.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDetailResponse {
    private Vehicle vehicle;
    private List<DriverInfo> drivers;
    private List<VehicleDocumentInfo> documents;
}

