package com.ProyectoPOO.ProyectoPOO.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;

/**
 * Controlador web para servir las páginas HTML de la aplicación frontend
 */
@Controller
@RequestMapping("/web")
public class WebController {

    /**
     * Servir la página de visualización de trayectos
     * GET /web/trayectos
     */
    @GetMapping("/trayectos")
    public ResponseEntity<String> trayectos() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/trayectos.html");
        String html;
        try (InputStream inputStream = resource.getInputStream(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            html = outputStream.toString(StandardCharsets.UTF_8);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /**
     * Página principal (dashboard)
     * GET /web/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/web/trayectos";
    }

    /**
     * Página de inicio
     * GET /web o GET /
     */
    @GetMapping({"", "/"})
    public ResponseEntity<String> index() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/index.html");
        String html;
        try (InputStream inputStream = resource.getInputStream(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            html = outputStream.toString(StandardCharsets.UTF_8);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}




