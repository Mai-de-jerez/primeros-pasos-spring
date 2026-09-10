package com.primera_app_spring.services;

import com.primera_app_spring.dto.NuevaPasswordDto;
import com.primera_app_spring.dto.SolicitarResetDto;
import com.primera_app_spring.model.PasswordResetToken;
import com.primera_app_spring.model.User;
import com.primera_app_spring.repository.PasswordResetTokenRepository;
import com.primera_app_spring.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User usuarioEjemplo;

    @BeforeEach
    void setUp() {
        usuarioEjemplo = new User();
        usuarioEjemplo.setEmail("may@ejemplo.com");
    }

    @Test
    @DisplayName("solicitarRecuperacion - Debe guardar token y enviar email si el usuario existe")
    void testSolicitarRecuperacionUsuarioExiste() {
        SolicitarResetDto dto = new SolicitarResetDto("may@ejemplo.com");
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(usuarioEjemplo));

        passwordResetService.solicitarRecuperacion(dto);

        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("solicitarRecuperacion - No debe guardar token ni enviar email si el usuario no existe")
    void testSolicitarRecuperacionUsuarioNoExiste() {
        SolicitarResetDto dto = new SolicitarResetDto("desconocido@ejemplo.com");
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

        passwordResetService.solicitarRecuperacion(dto);

        verify(tokenRepository, never()).save(any());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("cambiarPassword - Debe cambiar la contraseña y eliminar el token si todo es válido")
    void testCambiarPasswordExito() {
        NuevaPasswordDto dto = new NuevaPasswordDto("token-valido", "NuevaClave123", "NuevaClave123");
        PasswordResetToken tokenMock = spy(new PasswordResetToken("may@ejemplo.com", "token-valido"));

        when(tokenRepository.findByToken("token-valido")).thenReturn(Optional.of(tokenMock));
        when(tokenMock.isExpired(anyInt())).thenReturn(false);
        when(passwordEncoder.encode("NuevaClave123")).thenReturn("claveCifrada123");
        when(userRepository.updatePasswordByEmail("may@ejemplo.com", "claveCifrada123")).thenReturn(1);

        passwordResetService.cambiarPassword(dto);

        verify(userRepository, times(1)).updatePasswordByEmail("may@ejemplo.com", "claveCifrada123");
        verify(tokenRepository, times(1)).delete(tokenMock);
    }

    @Test
    @DisplayName("cambiarPassword - Debe lanzar excepción si las contraseñas no coinciden")
    void testCambiarPasswordContrasenasNoCoinciden() {
        NuevaPasswordDto dto = new NuevaPasswordDto("token-valido", "NuevaClave123", "OtraClave123");

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.cambiarPassword(dto));
        verify(tokenRepository, never()).findByToken(any());
    }

    @Test
    @DisplayName("cambiarPassword - Debe lanzar excepción y borrar token si el enlace ha caducado")
    void testCambiarPasswordTokenCaducado() {
        NuevaPasswordDto dto = new NuevaPasswordDto("token-caducado", "NuevaClave123", "NuevaClave123");
        PasswordResetToken tokenMock = spy(new PasswordResetToken("may@ejemplo.com", "token-caducado"));

        when(tokenRepository.findByToken("token-caducado")).thenReturn(Optional.of(tokenMock));
        when(tokenMock.isExpired(anyInt())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.cambiarPassword(dto));
        verify(tokenRepository, times(1)).delete(tokenMock);
        verify(userRepository, never()).updatePasswordByEmail(anyString(), anyString());
    }
}