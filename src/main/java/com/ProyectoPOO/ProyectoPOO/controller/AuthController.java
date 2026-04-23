package com.ProyectoPOO.ProyectoPOO.controller;

import com.ProyectoPOO.ProyectoPOO.dto.AuthRequest;
import com.ProyectoPOO.ProyectoPOO.security.PublicEndpoint;
import com.ProyectoPOO.ProyectoPOO.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@PublicEndpoint
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(userService.authenticate(request.getLogin(), request.getPassword()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }
}

