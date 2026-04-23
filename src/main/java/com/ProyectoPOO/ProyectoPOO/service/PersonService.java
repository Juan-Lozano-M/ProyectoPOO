package com.ProyectoPOO.ProyectoPOO.service;

import com.ProyectoPOO.ProyectoPOO.dto.AdminUserData;
import com.ProyectoPOO.ProyectoPOO.dto.PersonResponse;
import com.ProyectoPOO.ProyectoPOO.dto.PersonUpsertRequest;
import com.ProyectoPOO.ProyectoPOO.model.PersonType;
import com.ProyectoPOO.ProyectoPOO.model.Persona;
import com.ProyectoPOO.ProyectoPOO.model.Usuario;
import com.ProyectoPOO.ProyectoPOO.model.UsuarioId;
import com.ProyectoPOO.ProyectoPOO.repository.PersonaRepository;
import com.ProyectoPOO.ProyectoPOO.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordCodec passwordCodec;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public PersonResponse create(PersonUpsertRequest request) {
        validateRequest(request);
        if (personaRepository.existsByIdentification(request.getIdentification())) {
            throw new IllegalArgumentException("Ya existe una persona con esa identificacion");
        }
        if (personaRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe una persona con ese correo");
        }

        Persona persona = Persona.builder()
                .identification(request.getIdentification())
                .identificationType(request.getIdentificationType())
                .names(request.getNames())
                .lastNames(request.getLastNames())
                .email(request.getEmail())
                .personType(request.getPersonType())
                .build();

        Persona saved = personaRepository.save(persona);
        AdminUserData adminUserData = null;

        if (saved.getPersonType() == PersonType.A) {
            String login = buildLogin(saved.getNames(), saved.getLastNames(), saved.getIdentification());
            if (usuarioRepository.existsByIdLogin(login)) {
                throw new IllegalArgumentException("Ya existe un usuario con el login generado: " + login);
            }
            String rawPassword = generateRandomToken(12);
            String apiKey = generateRandomToken(28);

            Usuario user = Usuario.builder()
                    .id(UsuarioId.builder().personaId(saved.getId()).login(login).build())
                    .persona(saved)
                    .password(passwordCodec.hash(rawPassword))
                    .apikey(apiKey)
                    .build();
            usuarioRepository.save(user);

            adminUserData = AdminUserData.builder()
                    .login(login)
                    .generatedPassword(rawPassword)
                    .apiKey(apiKey)
                    .build();
        }

        return toResponse(saved, adminUserData);
    }

    @Transactional
    public PersonResponse update(Long id, PersonUpsertRequest request) {
        validateRequest(request);
        Persona existing = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));

        boolean duplicateIdentification = personaRepository.findByIdentification(request.getIdentification())
                .filter(p -> !p.getId().equals(id))
                .isPresent();
        if (duplicateIdentification) {
            throw new IllegalArgumentException("Ya existe otra persona con esa identificacion");
        }

        boolean duplicateEmail = personaRepository.findByEmail(request.getEmail())
                .filter(p -> !p.getId().equals(id))
                .isPresent();
        if (duplicateEmail) {
            throw new IllegalArgumentException("Ya existe otra persona con ese correo");
        }

        existing.setIdentification(request.getIdentification());
        existing.setIdentificationType(request.getIdentificationType());
        existing.setNames(request.getNames());
        existing.setLastNames(request.getLastNames());
        existing.setEmail(request.getEmail());

        if (request.getPersonType() != existing.getPersonType()) {
            if (request.getPersonType() == PersonType.A) {
                existing.setPersonType(PersonType.A);
                Persona saved = personaRepository.save(existing);
                String login = buildLogin(saved.getNames(), saved.getLastNames(), saved.getIdentification());
                if (usuarioRepository.existsByIdLogin(login)) {
                    throw new IllegalArgumentException("Ya existe un usuario con el login generado: " + login);
                }
                String rawPassword = generateRandomToken(12);
                String apiKey = generateRandomToken(28);

                Usuario user = Usuario.builder()
                        .id(UsuarioId.builder().personaId(saved.getId()).login(login).build())
                        .persona(saved)
                        .password(passwordCodec.hash(rawPassword))
                        .apikey(apiKey)
                        .build();
                usuarioRepository.save(user);

                return toResponse(saved, AdminUserData.builder()
                        .login(login)
                        .generatedPassword(rawPassword)
                        .apiKey(apiKey)
                        .build());
            }
            if (request.getPersonType() == PersonType.C) {
                existing.setPersonType(PersonType.C);
                usuarioRepository.findAll().stream()
                        .filter(u -> u.getPersona().getId().equals(existing.getId()))
                        .findFirst()
                        .ifPresent(usuarioRepository::delete);
            }
        }

        Persona saved = personaRepository.save(existing);
        return toResponse(saved, null);
    }

    public PersonResponse getById(Long id) {
        Persona person = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));
        return toResponse(person, null);
    }

    public List<PersonResponse> listAll() {
        return personaRepository.findAll().stream().map(p -> toResponse(p, null)).toList();
    }

    public Map<PersonType, Long> countByType() {
        return personaRepository.countByType().stream().collect(Collectors.toMap(
                row -> (PersonType) row[0],
                row -> (Long) row[1]
        ));
    }

    static String buildLogin(String names, String lastNames, String identification) {
        String nameInitial = names == null || names.isBlank() ? "x" : names.trim().substring(0, 1).toLowerCase();
        String lastNameInitial = lastNames == null || lastNames.isBlank() ? "x" : lastNames.trim().substring(0, 1).toLowerCase();
        return nameInitial + lastNameInitial + identification;
    }

    private String generateRandomToken(int bytes) {
        byte[] values = new byte[bytes];
        secureRandom.nextBytes(values);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(values);
    }

    private void validateRequest(PersonUpsertRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio");
        }
        if (request.getIdentification() == null || request.getIdentification().isBlank()) {
            throw new IllegalArgumentException("La identificacion es obligatoria");
        }
        if (request.getIdentificationType() == null) {
            throw new IllegalArgumentException("El tipo de identificacion es obligatorio");
        }
        if (request.getNames() == null || request.getNames().isBlank()) {
            throw new IllegalArgumentException("Los nombres son obligatorios");
        }
        if (request.getLastNames() == null || request.getLastNames().isBlank()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (request.getPersonType() == null) {
            throw new IllegalArgumentException("El tipo de persona es obligatorio");
        }
    }

    private PersonResponse toResponse(Persona persona, AdminUserData adminUserData) {
        return PersonResponse.builder()
                .id(persona.getId())
                .identification(persona.getIdentification())
                .names(persona.getNames())
                .lastNames(persona.getLastNames())
                .email(persona.getEmail())
                .personType(persona.getPersonType())
                .adminUserData(adminUserData)
                .build();
    }
}


