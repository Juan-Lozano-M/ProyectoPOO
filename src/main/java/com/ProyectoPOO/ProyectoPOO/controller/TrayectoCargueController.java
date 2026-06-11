package com.ProyectoPOO.ProyectoPOO.controller;

import com.ProyectoPOO.ProyectoPOO.dto.CargueResumenResponse;
import com.ProyectoPOO.ProyectoPOO.service.TrayectoCargueService;
import com.ProyectoPOO.ProyectoPOO.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador protegido para el cargue masivo de trayectos desde Excel.
 * Requiere: Token JWT + APIKey en headers
 */
@RestController
@RequestMapping("/api/trayectos/cargue")
@RequiredArgsConstructor
public class TrayectoCargueController {

    private final TrayectoCargueService trayectoCargueService;
    private final UserService userService;

    /**
     * Carga un archivo Excel con trayectos a la tabla de staging y ejecuta los
     * procedimientos de validación y procesado.
     * POST /api/trayectos/cargue
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> cargarExcel(
            @Parameter(description = "Plantilla Excel (.xlsx) con los trayectos a cargar",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true) @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        try {
            var usuario = userService.validateSecurityHeaders(authHeader, apiKey);
            String userLogin = usuario.getId().getLogin();

            CargueResumenResponse response = trayectoCargueService.cargarDesdeExcel(file, userLogin);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    /**
     * Consulta el resultado de un cargue previamente realizado.
     * GET /api/trayectos/cargue/{idCargue}
     */
    @GetMapping("/{idCargue}")
    public ResponseEntity<?> consultarCargue(
            @PathVariable Long idCargue,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true) @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        try {
            userService.validateSecurityHeaders(authHeader, apiKey);
            return ResponseEntity.ok(trayectoCargueService.consultarCargue(idCargue));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }
}
