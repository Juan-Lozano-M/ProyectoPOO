package com.ProyectoPOO.ProyectoPOO.controller;

import com.ProyectoPOO.ProyectoPOO.dto.ChangePasswordRequest;
import com.ProyectoPOO.ProyectoPOO.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/{login}/password")
    public ResponseEntity<?> changePassword(@PathVariable String login, @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(login, request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password actualizado"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/{login}/regenerate-apikey")
    public ResponseEntity<?> regenerateApiKey(@PathVariable String login) {
        try {
            String apiKey = userService.regenerateApiKey(login);
            return ResponseEntity.ok(Map.of("login", login, "apiKey", apiKey));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}

