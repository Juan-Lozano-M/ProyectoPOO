package com.ProyectoPOO.ProyectoPOO.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Base64;

@Entity
// Evita duplicar el mismo tipo de documento para el mismo vehiculo.
@Table(name = "vehicle_documents", uniqueConstraints = {@UniqueConstraint(columnNames = {"vehicle_id", "document_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // `vehicle` = vehiculo; muchos registros de esta tabla pertenecen a un solo vehiculo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    // Evita recursión infinita al serializar JSON en relación bidireccional.
    @JsonBackReference
    private Vehicle vehicle;

    // `document` = documento parametrico (SOAT, Tecnomecanica, etc.).
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // `issueDate` = fecha de expedicion del documento del vehiculo.
    private LocalDate issueDate;

    // `expiryDate` = fecha de vencimiento del documento del vehiculo.
    private LocalDate expiryDate;

    // `state` = estado del documento asociado al vehiculo.
    @Enumerated(EnumType.STRING)
    private DocumentState state;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "pdf_blob", columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] pdfBlob;

    @JsonProperty("pdfBase64")
    public String getPdfBase64() {
        if (pdfBlob == null || pdfBlob.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(pdfBlob);
    }

    @JsonProperty("pdfBase64")
    public void setPdfBase64(String pdfBase64) {
        if (pdfBase64 == null || pdfBase64.isBlank()) {
            this.pdfBlob = null;
            return;
        }
        this.pdfBlob = Base64.getDecoder().decode(pdfBase64);
    }

    // Callback de ciclo de vida: se ejecuta automáticamente en INSERT/UPDATE.
    @PrePersist
    @PreUpdate
    private void validateAndDefaults() {
        // if simple para default: cuando state es null, se asigna un valor por defecto.
        if (state == null) {
            state = DocumentState.EN_VERIFICACION;
        }

        // Guard clauses: cada if corta el flujo lanzando excepción si falta un dato obligatorio.
        if (vehicle == null) throw new IllegalArgumentException("El vehículo es obligatorio para el documento del vehículo");
        if (document == null) throw new IllegalArgumentException("El documento es obligatorio para el documento del vehículo");
        if (issueDate == null) throw new IllegalArgumentException("La fecha de expedición es obligatoria para el documento del vehículo");
        if (expiryDate == null) throw new IllegalArgumentException("La fecha de vencimiento es obligatoria para el documento del vehículo");
    }

}
