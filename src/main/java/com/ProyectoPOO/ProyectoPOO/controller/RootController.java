package com.ProyectoPOO.ProyectoPOO.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para la raíz de la aplicación
 */
@Controller
public class RootController {

    /**
     * Redirige la raíz "/" a la página de inicio en "/web"
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/web";
    }
}

