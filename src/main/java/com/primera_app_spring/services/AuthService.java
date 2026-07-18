package com.primera_app_spring.services;


import com.primera_app_spring.dto.RegistroDto;
import com.primera_app_spring.model.User;
import com.primera_app_spring.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registrarNuevoUsuario(RegistroDto dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("Ese nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Ese email ya está registrado");
        }

        User nuevoUsuario = new User(
                dto.username(),
                passwordEncoder.encode(dto.password()), 
                dto.email(),
                Set.of("ROLE_USER") 
        );

        userRepository.save(nuevoUsuario);
    }
}
