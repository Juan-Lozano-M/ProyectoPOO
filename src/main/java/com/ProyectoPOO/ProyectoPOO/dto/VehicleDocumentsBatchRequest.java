package com.ProyectoPOO.ProyectoPOO.dto;

import lombok.Data;

import java.util.List;

@Data
public class VehicleDocumentsBatchRequest {
    private List<VehicleDocumentUpsertItem> documents;
}

