package com.ProyectoPOO.ProyectoPOO.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VehicleDocumentUpsertItem {
    private Long documentId;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String pdfBase64;
}

