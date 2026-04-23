package com.ProyectoPOO.ProyectoPOO.dto;

import com.ProyectoPOO.ProyectoPOO.model.DocumentState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDocumentInfo {
    private Long documentId;
    private String code;
    private String name;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private DocumentState state;
}

