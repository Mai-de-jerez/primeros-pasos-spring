package com.primera_app_spring.services;

import com.primera_app_spring.dto.RegistroDto;
import com.primera_app_spring.model.User;
import com.primera_app_spring.repository.UserRepository;
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

    @InjectMocks
    private AuthService authService;

    @Test
    void cuandoRegistroEsExitoso_entoncesGuardaUsuarioCorrectamente() {
        // 1. Arrange (Preparar datos) - Usamos la contraseña válida
        RegistroDto dto = new RegistroDto("May", "may@email.com", "Password123", "Password123");
        
        Mockito.when(userRepository.existsByUsername("May")).thenReturn(false);
        Mockito.when(userRepository.existsByEmail("may@email.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("Password123")).thenReturn("passwordEncriptada123");

        // 2. Act (Ejecutar la acción)
        authService.registrarNuevoUsuario(dto);

        // 3. Assert (Verificar que se llamó al método save del repositorio)
        Mockito.verify(userRepository, Mockito.times(1)).save(any(User.class));
    }

    @Test
    void cuandoUsernameYaExiste_entoncesLanzaIllegalArgumentException() {

        RegistroDto dto = new RegistroDto("May", "may@email.com", "Password123", "Password123");
        Mockito.when(userRepository.existsByUsername("May")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException excepcion = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            authService.registrarNuevoUsuario(dto);
        });

        Assertions.assertEquals("Ese nombre de usuario ya está en uso", excepcion.getMessage());
        // Nos aseguramos de que NUNCA intente guardar si salta este error
        Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void cuandoEmailYaExiste_entoncesLanzaIllegalArgumentException() {
        // Arrange - Mantenemos coherencia con la contraseña en todos los tests
        RegistroDto dto = new RegistroDto("May", "may@email.com", "Password123", "Password123");
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