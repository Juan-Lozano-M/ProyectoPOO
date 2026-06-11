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
public class TrayectoTmpResponse {

    private Long id;
    private Long idCargue;
    private String estado;
    private String observacion;
    private String personaIdentification;
    private String vehiclePlate;
    private String routeCode;
    private String location;
    private String stopOrder;
    private String latitude;
    private String longitude;
    private String registeredByLogin;
    private LocalDateTime createdAt;
}
