package com.ProyectoPOO.ProyectoPOO.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDate;

@Entity
@Table(name = "vehicle_documents", uniqueConstraints = {@UniqueConstraint(columnNames = {"vehicle_id", "document_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonBackReference
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private DocumentState state;

    @PrePersist
    @PreUpdate
    private void validateAndDefaults() {
        // estado por defecto
        if (state == null) {
            state = DocumentState.EN_VERIFICACION;
        }
        // validaciones simples
        if (vehicle == null) throw new IllegalArgumentException("El vehículo es obligatorio para el documento del vehículo");
        if (document == null) throw new IllegalArgumentException("El documento es obligatorio para el documento del vehículo");
        if (issueDate == null) throw new IllegalArgumentException("La fecha de expedición es obligatoria para el documento del vehículo");
        if (expiryDate == null) throw new IllegalArgumentException("La fecha de vencimiento es obligatoria para el documento del vehículo");
    }

}
