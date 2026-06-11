package com.ProyectoPOO.ProyectoPOO.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrayectoRequest {
    
    // ID del conductor (persona)
    private Long personaId;
    
    // ID del vehículo
    private Long vehicleId;
    
    // Código de ruta
    private String routeCode;
    
    // Ubicación de la parada
    private String location;
    
    // Orden de la parada
    private Integer stopOrder;
    
    // Coordenadas (opcionales en creación, se pueden obtener de Google Maps)
    private Double latitude;
    private Double longitude;
}

