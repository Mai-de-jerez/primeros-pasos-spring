package com.primera_app_spring.services;

import com.primera_app_spring.dto.EditarPerfilDto;
import com.primera_app_spring.model.User;
import com.primera_app_spring.repository.UserRepository;
import com.primera_app_spring.storage.StorageService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, StorageService storageService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.passwordEncoder = passwordEncoder;
    }

    public User buscarPorUsername(String username) {
        log.info("Buscando los datos del usuario '{}' en la base de datos", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }
    
    @Transactional
    public User actualizarPerfil(String usernameActual, EditarPerfilDto dto) {
        log.info("Actualizando perfil para el usuario: '{}'", usernameActual);

        User usuario = buscarPorUsername(usernameActual);

        // Si cambia el username, verificamos que no esté en uso por otro
        if (!usuario.getUsername().equals(dto.username())) {
            if (userRepository.existsByUsername(dto.username())) {
                throw new IllegalArgumentException("Ese nombre de usuario ya está en uso");
            }
            usuario.setUsername(dto.username());
        }

        // Si cambia el email, verificamos que no esté registrado por otro
        if (!usuario.getEmail().equals(dto.email())) {
            if (userRepository.existsByEmail(dto.email())) {
                throw new IllegalArgumentException("Ese email ya está registrado");
            }
            usuario.setEmail(dto.email());
        }

        // Si introduce una nueva contraseña, validamos que coincidan y la actualizamos
        if (dto.password() != null && !dto.password().isBlank()) {
            if (!dto.password().equals(dto.confirmPassword())) {
                throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
            }
            usuario.setPassword(passwordEncoder.encode(dto.password()));
            log.info("Contraseña actualizada para el usuario: '{}'", usuario.getUsername());
        }

        // Procesamos la foto nueva si se adjunta una
        MultipartFile nuevaFoto = dto.foto();
        if (nuevaFoto != null && !nuevaFoto.isEmpty()) {
            try {
                if (usuario.getFoto() != null) {
                    try {
                        storageService.delete(usuario.getFoto());
                    } catch (Exception e) {
                        log.warn("No se pudo eliminar la foto antigua: {}", e.getMessage());
                    }
                }

                String nombreFichero = storageService.store(nuevaFoto, usuario.getId());
                usuario.setFoto(nombreFichero);
                log.info("Nueva foto de perfil guardada para el usuario ID: {}", usuario.getId());
            } catch (Exception e) {
                log.error("Error al procesar la nueva foto de perfil", e);
                throw new RuntimeException("Error al guardar la imagen: " + e.getMessage());
            }
        }

        userRepository.save(usuario);
        log.info("Perfil actualizado correctamente para '{}'", usuario.getUsername());
        return usuario;
    }
}

