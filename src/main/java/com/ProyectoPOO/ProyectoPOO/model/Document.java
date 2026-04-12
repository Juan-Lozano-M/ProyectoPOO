package com.ProyectoPOO.ProyectoPOO.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

@Entity
// `code` = codigo parametrizado; no se puede repetir.
@Table(name = "documents", uniqueConstraints = {@UniqueConstraint(columnNames = {"code"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Restringe valores válidos de aplicabilidad y obligatoriedad a nivel BD.
@Check(constraints = "applicability IN ('A','M','AM') AND mandatory IN ('RA','RM','RR')")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // `code` = codigo unico del documento (ejemplo: SOAT, TM, STR).
    @Column(nullable = false, length = 50)
    private String code;

    // `name` = nombre del documento.
    @Column(nullable = false)
    private String name;

    // `applicability` = aplicabilidad (A, M o AM).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentApplicability applicability;

    // `mandatory` = obligatoriedad (RA, RM o RR).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentMandatory mandatory;

    // `description` = descripcion del documento parametrizado.
    private String description;

    // JPA llama este método antes de persistir o actualizar la entidad.
    @PrePersist
    @PreUpdate
    private void validate() {
        // Flujo de validación previo a persistir/actualizar:
        // 1) Identidad funcional del documento (código y nombre).
        // 2) Reglas de negocio paramétricas (aplicabilidad y obligatoriedad).
        // Si falla cualquiera, se aborta la operación antes de tocar BD.
        // En cada if se usa || para validar null primero y luego contenido (short-circuit).
        if (code == null || code.isBlank()) throw new IllegalArgumentException("El código del documento es obligatorio");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("El nombre del documento es obligatorio");
        if (applicability == null) throw new IllegalArgumentException("La aplicabilidad del documento es obligatoria");
        if (mandatory == null) throw new IllegalArgumentException("La obligatoriedad del documento es obligatoria");
    }

}
