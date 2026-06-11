package com.ProyectoPOO.ProyectoPOO.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrayectoResponse {
    
    private Long id;
    private Long personaId;
    private String conductorNombre;
    private String conductorApellido;
    private Long vehicleId;
    private String vehiclePlate;
    private String routeCode;
    private String location;
    private Integer stopOrder;
    private Double latitude;
    private Double longitude;
    private String registeredByLogin;
    private LocalDateTime createdAt;
}

