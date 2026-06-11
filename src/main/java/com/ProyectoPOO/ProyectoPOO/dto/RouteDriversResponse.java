package com.ProyectoPOO.ProyectoPOO.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteDriversResponse {
    private String routeCode;
    private List<DriverInfo> drivers;
}

