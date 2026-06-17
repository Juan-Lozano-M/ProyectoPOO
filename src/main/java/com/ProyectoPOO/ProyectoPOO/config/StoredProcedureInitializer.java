package com.ProyectoPOO.ProyectoPOO.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Crea (o reemplaza) los procedimientos almacenados usados por el cargue masivo de
 * trayectos: cargue, validación y procesado de la tabla de staging trayecto_tmp.
 * Se ejecuta al arrancar la aplicación, después de que Hibernate crea/actualiza el
 * esquema (ddl-auto=update), por lo que las tablas trayecto_tmp y trayectos ya existen.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(Integer.MAX_VALUE)
public class StoredProcedureInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Creando/actualizando procedimientos almacenados de cargue de trayectos...");

        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS sp_cargue_trayecto");
        jdbcTemplate.execute(SP_CARGUE_TRAYECTO);

        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS sp_validar_trayecto");
        jdbcTemplate.execute(SP_VALIDAR_TRAYECTO);

        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS sp_procesar_trayecto");
        jdbcTemplate.execute(SP_PROCESAR_TRAYECTO);

        log.info("Procedimientos almacenados de cargue de trayectos listos");
    }

    private static final String SP_CARGUE_TRAYECTO = """
            CREATE PROCEDURE sp_cargue_trayecto(
                IN p_id_cargue BIGINT,
                IN p_persona_identification VARCHAR,
                IN p_vehicle_plate VARCHAR,
                IN p_route_code VARCHAR,
                IN p_location TEXT,
                IN p_stop_order VARCHAR,
                IN p_latitude VARCHAR,
                IN p_longitude VARCHAR,
                IN p_registered_by_login VARCHAR
            )
            LANGUAGE plpgsql
            AS $$
            BEGIN
                INSERT INTO trayecto_tmp (
                    id_cargue, estado, observacion, persona_identification, vehicle_plate,
                    route_code, location, stop_order, latitude, longitude, registered_by_login, created_at
                ) VALUES (
                    p_id_cargue, 'CARGADO', NULL, p_persona_identification, p_vehicle_plate,
                    p_route_code, p_location, p_stop_order, p_latitude, p_longitude, p_registered_by_login, NOW()
                );
            END;
            $$;
            """;

    private static final String SP_VALIDAR_TRAYECTO = """
            CREATE PROCEDURE sp_validar_trayecto(IN p_id_cargue BIGINT)
            LANGUAGE plpgsql
            AS $$
            DECLARE
                v_id BIGINT;
                v_persona_identification VARCHAR(30);
                v_vehicle_plate VARCHAR(6);
                v_route_code VARCHAR(50);
                v_location TEXT;
                v_stop_order VARCHAR(20);
                v_latitude VARCHAR(30);
                v_longitude VARCHAR(30);

                v_persona_id BIGINT;
                v_vehicle_id BIGINT;
                v_person_type VARCHAR(1);
                v_driver_state VARCHAR(2);
                v_doc_count INT;
                v_doc_not_enabled INT;
                v_observacion TEXT;
                v_stop_order_num INT;
                v_lat_num DECIMAL(20,10);
                v_lon_num DECIMAL(20,10);
            BEGIN
                v_id := (SELECT MIN(id) FROM trayecto_tmp WHERE id_cargue = p_id_cargue AND estado = 'CARGADO');

                WHILE v_id IS NOT NULL LOOP
                    SELECT persona_identification, vehicle_plate, route_code, location, stop_order, latitude, longitude
                        INTO v_persona_identification, v_vehicle_plate, v_route_code, v_location, v_stop_order, v_latitude, v_longitude
                        FROM trayecto_tmp WHERE id = v_id;

                    v_observacion := '';
                    v_persona_id := NULL;
                    v_vehicle_id := NULL;
                    v_person_type := NULL;
                    v_driver_state := NULL;

                    SELECT id, person_type INTO v_persona_id, v_person_type
                        FROM personas WHERE identification = v_persona_identification LIMIT 1;
                    IF v_persona_id IS NULL THEN
                        v_observacion := CONCAT(v_observacion, 'Conductor no encontrado por identificacion; ');
                    ELSIF v_person_type <> 'C' THEN
                        v_observacion := CONCAT(v_observacion, 'La persona no es un conductor (tipo C); ');
                    END IF;

                    SELECT id INTO v_vehicle_id FROM vehicles WHERE plate = v_vehicle_plate LIMIT 1;
                    IF v_vehicle_id IS NULL THEN
                        v_observacion := CONCAT(v_observacion, 'Vehiculo no encontrado por placa; ');
                    END IF;

                    IF v_persona_id IS NOT NULL AND v_vehicle_id IS NOT NULL THEN
                        SELECT state INTO v_driver_state FROM vehicle_drivers
                            WHERE persona_id = v_persona_id AND vehicle_id = v_vehicle_id LIMIT 1;
                        IF v_driver_state IS NULL THEN
                            v_observacion := CONCAT(v_observacion, 'El conductor no esta asociado al vehiculo; ');
                        ELSIF v_driver_state <> 'PO' THEN
                            v_observacion := CONCAT(v_observacion, 'El conductor no puede operar el vehiculo (estado ', v_driver_state, '); ');
                        END IF;
                    END IF;

                    IF v_vehicle_id IS NOT NULL THEN
                        SELECT COUNT(*) INTO v_doc_count FROM vehicle_documents WHERE vehicle_id = v_vehicle_id;
                        SELECT COUNT(*) INTO v_doc_not_enabled FROM vehicle_documents
                            WHERE vehicle_id = v_vehicle_id AND state <> 'HABILITADO';
                        IF v_doc_count = 0 OR v_doc_not_enabled > 0 THEN
                            v_observacion := CONCAT(v_observacion, 'No todos los documentos del vehiculo estan HABILITADO; ');
                        END IF;
                    END IF;

                    IF v_route_code IS NULL OR TRIM(v_route_code) = '' THEN
                        v_observacion := CONCAT(v_observacion, 'El codigo de ruta es obligatorio; ');
                    END IF;
                    IF v_location IS NULL OR TRIM(v_location) = '' THEN
                        v_observacion := CONCAT(v_observacion, 'La ubicacion es obligatoria; ');
                    END IF;

                    IF v_stop_order IS NULL OR TRIM(v_stop_order) = '' OR v_stop_order !~ '^[0-9]+$' THEN
                        v_observacion := CONCAT(v_observacion, 'El orden de parada debe ser un numero entero >= 0; ');
                    ELSE
                        v_stop_order_num := CAST(v_stop_order AS INTEGER);
                        IF v_stop_order_num > 5 AND v_stop_order_num <> 99 THEN
                            v_observacion := CONCAT(v_observacion, 'Maximo 5 paradas intermedias permitidas (orden 0-5 o 99 para final); ');
                        END IF;
                    END IF;

                    IF v_latitude IS NOT NULL AND TRIM(v_latitude) <> '' THEN
                        IF v_latitude !~ '^-?[0-9]+(.[0-9]+)?$' THEN
                            v_observacion := CONCAT(v_observacion, 'La latitud no es un numero valido; ');
                        ELSE
                            v_lat_num := CAST(v_latitude AS DECIMAL(20,10));
                            IF v_lat_num < -90 OR v_lat_num > 90 THEN
                                v_observacion := CONCAT(v_observacion, 'La latitud debe estar entre -90 y 90; ');
                            END IF;
                        END IF;
                    END IF;

                    IF v_longitude IS NOT NULL AND TRIM(v_longitude) <> '' THEN
                        IF v_longitude !~ '^-?[0-9]+(.[0-9]+)?$' THEN
                            v_observacion := CONCAT(v_observacion, 'La longitud no es un numero valido; ');
                        ELSE
                            v_lon_num := CAST(v_longitude AS DECIMAL(20,10));
                            IF v_lon_num < -180 OR v_lon_num > 180 THEN
                                v_observacion := CONCAT(v_observacion, 'La longitud debe estar entre -180 y 180; ');
                            END IF;
                        END IF;
                    END IF;

                    IF v_observacion = '' THEN
                        UPDATE trayecto_tmp SET estado = 'VALIDADO', observacion = NULL WHERE id = v_id;
                    ELSE
                        UPDATE trayecto_tmp SET estado = 'ERROR', observacion = v_observacion WHERE id = v_id;
                    END IF;

                    v_id := (SELECT MIN(id) FROM trayecto_tmp WHERE id_cargue = p_id_cargue AND estado = 'CARGADO');
                END LOOP;
            END;
            $$;
            """;

    private static final String SP_PROCESAR_TRAYECTO = """
            CREATE PROCEDURE sp_procesar_trayecto(IN p_id_cargue BIGINT)
            LANGUAGE plpgsql
            AS $$
            DECLARE
                v_id BIGINT;
                v_persona_identification VARCHAR(30);
                v_vehicle_plate VARCHAR(6);
                v_route_code VARCHAR(50);
                v_location TEXT;
                v_stop_order VARCHAR(20);
                v_latitude VARCHAR(30);
                v_longitude VARCHAR(30);
                v_registered_by_login VARCHAR(50);
                v_persona_id BIGINT;
                v_vehicle_id BIGINT;
            BEGIN
                v_id := (SELECT MIN(id) FROM trayecto_tmp WHERE id_cargue = p_id_cargue AND estado = 'VALIDADO');

                WHILE v_id IS NOT NULL LOOP
                    SELECT persona_identification, vehicle_plate, route_code, location, stop_order, latitude, longitude, registered_by_login
                        INTO v_persona_identification, v_vehicle_plate, v_route_code, v_location, v_stop_order, v_latitude, v_longitude, v_registered_by_login
                        FROM trayecto_tmp WHERE id = v_id;

                    v_persona_id := NULL;
                    v_vehicle_id := NULL;
                    SELECT id INTO v_persona_id FROM personas WHERE identification = v_persona_identification LIMIT 1;
                    SELECT id INTO v_vehicle_id FROM vehicles WHERE plate = v_vehicle_plate LIMIT 1;

                    INSERT INTO trayectos (persona_id, vehicle_id, route_code, location, stop_order, latitude, longitude, registered_by_login, created_at)
                    VALUES (
                        v_persona_id, v_vehicle_id, v_route_code, v_location,
                        CAST(v_stop_order AS INTEGER),
                        CASE WHEN v_latitude IS NULL OR TRIM(v_latitude) = '' THEN NULL ELSE CAST(v_latitude AS DECIMAL(20,10)) END,
                        CASE WHEN v_longitude IS NULL OR TRIM(v_longitude) = '' THEN NULL ELSE CAST(v_longitude AS DECIMAL(20,10)) END,
                        v_registered_by_login, NOW()
                    );

                    UPDATE trayecto_tmp SET estado = 'PROCESADO' WHERE id = v_id;

                    v_id := (SELECT MIN(id) FROM trayecto_tmp WHERE id_cargue = p_id_cargue AND estado = 'VALIDADO');
                END LOOP;
            END;
            $$;
            """;
}
