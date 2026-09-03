package com.primera_app_spring.services;

import com.primera_app_spring.dto.RegistroDto;
import com.primera_app_spring.model.User;
import com.primera_app_spring.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.primera_app_spring.storage.StorageService;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;

@Service
public class AuthService {
	
	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, StorageService storageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
    }

    
    
    /**
     * Registra un nuevo usuario en el sistema aplicando las reglas de negocio de registro.
     * Cifra la contraseña del usuario utilizando el componente criptográfico configurado
     * y le asigna el rol por defecto de cliente (ROLE_USER) antes de su persistencia.
     * @param dto el objeto de transferencia de datos (nombre de usuario, correo electrónico, contraseña y foto)
     * @throws IllegalArgumentException lanza una excepción si el nombre de usuario o el email ya existen.
     */
    @Transactional
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

        // 1. Guardamos primero el usuario con la foto a null para que obtenga su ID autogenerado
        User nuevoUsuario = new User(
                dto.username(),
                passwordEncoder.encode(dto.password()), 
                dto.email(),
                null, // La foto se asigna después de almacenar el fichero
                Set.of("ROLE_USER") 
        );

        User usuarioGuardado = userRepository.save(nuevoUsuario);

        // 2. Procesamos la foto si viene adjunta en el DTO
        MultipartFile foto = dto.foto();
        if (foto != null && !foto.isEmpty()) {
            String nombreFichero = null;
            try {
                // Almacenamos el fichero usando el ID del usuario como nombre
                nombreFichero = storageService.store(foto, usuarioGuardado.getId());
                
                // Actualizamos el usuario con el nombre del fichero guardado
                usuarioGuardado.setFoto(nombreFichero);
                userRepository.save(usuarioGuardado);
                
                log.info("Foto de perfil guardada con éxito para el usuario ID: {}", usuarioGuardado.getId());
            } catch (Exception e) {
                log.error("Error al guardar la foto de perfil. Limpiando archivo huérfano...", e);
                
                // Si el fichero llegó a crearse físicamente pero algo falló después, lo borramos para que no quede huérfano
                if (nombreFichero != null) {
                    try {
                        storageService.delete(nombreFichero);
                    } catch (Exception ex) {
                        log.error("No se pudo eliminar el archivo huérfano: {}", ex.getMessage());
                    }
                }
                
                // Lanzamos excepción para activar el @Transactional y deshacer el guardado en BBDD
                throw new RuntimeException("Error al procesar la imagen de perfil: " + e.getMessage());
            }
        }
      
        log.info("Usuario '{}' registrado correctamente con el rol ROLE_USER", dto.username());
    }
}

