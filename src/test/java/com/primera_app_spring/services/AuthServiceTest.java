package com.primera_app_spring.services;

import com.primera_app_spring.dto.RegistroDto;
import com.primera_app_spring.model.User;
import com.primera_app_spring.repository.UserRepository;
import com.primera_app_spring.storage.StorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StorageService storageService; // Añadido el mock del StorageService

    @InjectMocks
    private AuthService authService;

    @Test
    void cuandoRegistroEsExitoso_entoncesGuardaUsuarioCorrectamente() {
        // 1. Arrange - 5 argumentos (el último es la foto a null)
        RegistroDto dto = new RegistroDto("May", "may@email.com", "Password123", "Password123", null);
        
        Mockito.when(userRepository.existsByUsername("May")).thenReturn(false);
        Mockito.when(userRepository.existsByEmail("may@email.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("Password123")).thenReturn("passwordEncriptada123");

        // Simulamos que al guardar el usuario en BBDD por primera vez, devuelve una instancia con ID 1L
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            // Creamos un usuario simulado con ID asignado
            User savedUser = new User(
                userToSave.getUsername(), 
                userToSave.getPassword(), 
                userToSave.getEmail(), 
                userToSave.getFoto(), 
                userToSave.getRoles()
            );
            // Usamos Mockito o un builder/setter si tienes el ID accesible, o simplemente verificamos que se llama
            return savedUser;
        });

        // 2. Act
        authService.registrarNuevoUsuario(dto);

        // 3. Assert - Se llama dos veces al save (una para crear el usuario sin foto y otra para actualizar con la foto, o una si no hay foto)
        Mockito.verify(userRepository, Mockito.atLeastOnce()).save(any(User.class));
    }

    @Test
    void cuandoUsernameYaExiste_entoncesLanzaIllegalArgumentException() {
        RegistroDto dto = new RegistroDto("May", "may@email.com", "Password123", "Password123", null);
        Mockito.when(userRepository.existsByUsername("May")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException excepcion = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            authService.registrarNuevoUsuario(dto);
        });

        Assertions.assertEquals("Ese nombre de usuario ya está en uso", excepcion.getMessage());
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void cuandoEmailYaExiste_entoncesLanzaIllegalArgumentException() {
        RegistroDto dto = new RegistroDto("May", "may@email.com", "Password123", "Password123", null);
        Mockito.when(userRepository.existsByUsername("May")).thenReturn(false);
        Mockito.when(userRepository.existsByEmail("may@email.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException excepcion = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            authService.registrarNuevoUsuario(dto);
        });

        Assertions.assertEquals("Ese email ya está registrado", excepcion.getMessage());
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }
}