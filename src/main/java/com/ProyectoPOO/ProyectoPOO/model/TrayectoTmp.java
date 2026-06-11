package com.ProyectoPOO.ProyectoPOO.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

/**
 * Tabla de staging para el cargue masivo de trayectos desde un archivo Excel.
 * Los campos réplica de la entidad transaccional Trayecto se almacenan como texto
 * para admitir cualquier valor proveniente del Excel sin alterarlo durante el cargue;
 * la validación y el formato correcto se verifican en los procedimientos almacenados.
 */
@Entity
@Table(name = "trayecto_tmp")
@Check(constraints = "estado IN ('CARGADO','VALIDADO','PROCESADO','ERROR')")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrayectoTmp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificador del cargue (yyyyMMddHHmm) que agrupa los registros de un mismo Excel
    @Column(name = "id_cargue", nullable = false)
    private Long idCargue;

    // Estado del registro: CARGADO, VALIDADO, PROCESADO o ERROR
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private TrayectoTmpState estado;

    // Descripción acumulativa de errores/restricciones encontradas en la validación
    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    // Réplicas de los campos de Trayecto, como texto (compatibles con el archivo Excel)
    @Column(name = "persona_identification", length = 30)
    private String personaIdentification;

    @Column(name = "vehicle_plate", length = 6)
    private String vehiclePlate;

    @Column(name = "route_code", length = 50)
    private String routeCode;

    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    @Column(name = "stop_order", length = 20)
    private String stopOrder;

    @Column(name = "latitude", length = 30)
    private String latitude;

    @Column(name = "longitude", length = 30)
    private String longitude;

    @Column(name = "registered_by_login", length = 50)
    private String registeredByLogin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (estado == null) {
            estado = TrayectoTmpState.CARGADO;
        }
    }
}
