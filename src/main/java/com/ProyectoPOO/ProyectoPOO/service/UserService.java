package com.ProyectoPOO.ProyectoPOO.service;

import com.ProyectoPOO.ProyectoPOO.dto.AuthResponse;
import com.ProyectoPOO.ProyectoPOO.model.PersonType;
import com.ProyectoPOO.ProyectoPOO.model.Usuario;
import com.ProyectoPOO.ProyectoPOO.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordCodec passwordCodec;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void changePassword(String login, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("La nueva contrasena es obligatoria");
        }
        Usuario usuario = usuarioRepository.findByIdLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        validateAdminUser(usuario);
        usuario.setPassword(passwordCodec.hash(newPassword));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public String regenerateApiKey(String login) {
        Usuario usuario = usuarioRepository.findByIdLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        validateAdminUser(usuario);
        String apiKey = generateRandomToken(28);
        usuario.setApikey(apiKey);
        usuarioRepository.save(usuario);
        return apiKey;
    }

    @Transactional
    public AuthResponse authenticate(String login, String rawPassword) {
        Usuario usuario = usuarioRepository.findByIdLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas"));
        validateAdminUser(usuario);
        if (!passwordCodec.matches(rawPassword, usuario.getPassword())) {
            throw new IllegalArgumentException("Credenciales invalidas");
        }
        String token = generateRandomToken(32);
        usuario.setTokenValue(token);
        usuarioRepository.save(usuario);

        return AuthResponse.builder()
                .token(token)
                .apiKey(usuario.getApikey())
                .login(usuario.getId().getLogin())
                .build();
    }

    @Transactional(readOnly = true)
    public Usuario validateSecurityHeaders(String authHeader, String apiKey) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token no enviado o invalido");
        }
        String token = authHeader.substring(7);
        Usuario userByToken = usuarioRepository.findByTokenValue(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalido"));
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("APIKey no enviada");
        }
        if (!apiKey.equals(userByToken.getApikey())) {
            throw new IllegalArgumentException("APIKey invalida");
        }
        validateAdminUser(userByToken);
        return userByToken;
    }

    private void validateAdminUser(Usuario usuario) {
        if (usuario.getPersona() == null || usuario.getPersona().getPersonType() != PersonType.A) {
            throw new IllegalArgumentException("Solo usuarios administrativos pueden autenticarse");
        }
    }

    private String generateRandomToken(int bytes) {
        byte[] values = new byte[bytes];
        secureRandom.nextBytes(values);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(values);
    }
}



