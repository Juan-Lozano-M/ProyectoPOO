package com.ProyectoPOO.ProyectoPOO.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CargueResumenResponse {

    private Long idCargue;
    private int totalRegistros;
    private int cargados;
    private int validados;
    private int procesados;
    private int conError;
    private List<TrayectoTmpResponse> registros;
}
