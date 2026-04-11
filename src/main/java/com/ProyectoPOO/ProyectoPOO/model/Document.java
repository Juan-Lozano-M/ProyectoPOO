package com.ProyectoPOO.ProyectoPOO.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "documents", uniqueConstraints = {@UniqueConstraint(columnNames = {"code"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Check(constraints = "applicability IN ('A','M','AM') AND mandatory IN ('RA','RM','RR')")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentApplicability applicability;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentMandatory mandatory;

    private String description;

    @PrePersist
    @PreUpdate
    private void validate() {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("El código del documento es obligatorio");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("El nombre del documento es obligatorio");
        if (applicability == null) throw new IllegalArgumentException("La aplicabilidad del documento es obligatoria");
        if (mandatory == null) throw new IllegalArgumentException("La obligatoriedad del documento es obligatoria");
    }

}
