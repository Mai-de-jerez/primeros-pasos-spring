package com.primera_app_spring.services;

import com.primera_app_spring.dto.NuevaPasswordDto;
import com.primera_app_spring.dto.SolicitarResetDto;
import com.primera_app_spring.model.PasswordResetToken;
import com.primera_app_spring.model.User;
import com.primera_app_spring.repository.PasswordResetTokenRepository;
import com.primera_app_spring.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final int EXPIRATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public PasswordResetService(UserRepository userRepository,
                                  PasswordResetTokenRepository tokenRepository,
                                  PasswordEncoder passwordEncoder,
                                  JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public void solicitarRecuperacion(SolicitarResetDto dto) {
        Optional<User> userOpt = userRepository.findByEmail(dto.email());

        if (userOpt.isEmpty()) {
            return; // no revelamos si existe el email
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(dto.email(), token);
        tokenRepository.save(resetToken);

        enviarEmailRecuperacion(dto.email(), token);
    }

    private void enviarEmailRecuperacion(String destinatario, String token) {
        String enlace = "http://localhost:8082/reset-password?token=" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Recuperación de contraseña");
        mensaje.setText(
                "Hola,\n\n" +
                "Has solicitado restablecer tu contraseña.\n" +
                "Haz clic en el siguiente enlace (válido " + EXPIRATION_MINUTES + " minutos):\n\n" +
                enlace + "\n\n" +
                "Si no has sido tú, ignora este mensaje."
        );

        mailSender.send(mensaje);
    }

    
	/*
	 * @Transactional public void cambiarPassword(NuevaPasswordDto dto) { if
	 * (!dto.password().equals(dto.confirmPassword())) { throw new
	 * IllegalArgumentException("Las contraseñas no coinciden"); }
	 * 
	 * PasswordResetToken resetToken = tokenRepository.findByToken(dto.token())
	 * .orElseThrow(() -> new IllegalArgumentException("El enlace no es válido"));
	 * 
	 * if (resetToken.isExpired(EXPIRATION_MINUTES)) {
	 * tokenRepository.delete(resetToken); throw new
	 * IllegalArgumentException("El enlace ha caducado, solicita uno nuevo"); }
	 * 
	 * User usuario = userRepository.findByEmail(resetToken.getEmail())
	 * .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
	 * 
	 * usuario.setPassword(passwordEncoder.encode(dto.password()));
	 * userRepository.save(usuario); tokenRepository.delete(resetToken); }
	 */
    
    @Transactional
    public void cambiarPassword(NuevaPasswordDto dto) {
        if (!dto.password().equals(dto.confirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(dto.token())
                .orElseThrow(() -> new IllegalArgumentException("El enlace no es válido"));

        if (resetToken.isExpired(EXPIRATION_MINUTES)) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("El enlace ha caducado, solicita uno nuevo");
        }

        String passwordEncriptada = passwordEncoder.encode(dto.password());
        int filasActualizadas = userRepository.updatePasswordByEmail(resetToken.getEmail(), passwordEncriptada);

        if (filasActualizadas == 0) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        tokenRepository.delete(resetToken);
    }
}