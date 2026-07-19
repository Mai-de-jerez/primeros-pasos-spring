package com.primera_app_spring.services;

import com.primera_app_spring.dto.RegistroDto;
import com.primera_app_spring.model.User;
import com.primera_app_spring.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;

@Service
public class AuthService {
	
	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario en el sistema aplicando las reglas de negocio de registro.
     * Cifra la contraseña del usuario utilizando el componente criptográfico configurado
     * y le asigna el rol por defecto de cliente (ROLE_USER) antes de su persistencia.
     * @param dto el objeto de transferencia de datos (nombre de usuario, correo electrónico y contraseña)
     * @throws IllegalArgumentException lanaza una excepción si el nombre de usuario ya se 
     * encuentra registrado en el sistema, o si la dirección de correo electrónico ya existee n la db.
     */
    public void registrarNuevoUsuario(RegistroDto dto) {

        log.info("Iniciando intento de registro para el usuario: '{}' con email: '{}'", dto.username(), dto.email());

        if (userRepository.existsByUsername(dto.username())) {

            log.warn("Registro rechazado: El nombre de usuario '{}' ya existe en el sistema", dto.username());
            throw new IllegalArgumentException("Ese nombre de usuario ya está en uso");
        }
        
        if (userRepository.existsByEmail(dto.email())) {

            log.warn("Registro rechazado: El email '{}' ya está registrado en el sistema", dto.email());
            throw new IllegalArgumentException("Ese email ya está registrado");
        }

        User nuevoUsuario = new User(
                dto.username(),
                passwordEncoder.encode(dto.password()), 
                dto.email(),
                Set.of("ROLE_USER") 
        );

        userRepository.save(nuevoUsuario);
      
        log.info("Usuario '{}' registrado correctamente con el rol ROLE_USER", dto.username());
    }
}
