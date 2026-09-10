package com.primera_app_spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.primera_app_spring.dto.NuevaPasswordDto;
import com.primera_app_spring.dto.RegistroDto;
import com.primera_app_spring.dto.SolicitarResetDto;
import com.primera_app_spring.services.AuthService;
import com.primera_app_spring.services.PasswordResetService;

import jakarta.validation.Valid; 

@Controller
public class AuthController {
	
	private static final Logger log = LoggerFactory.getLogger(AuthController.class);
	
	private final AuthService authService;
	private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }


    /**
     * Muestra el formulario de registro de nuevos usuarios.
     */
    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        log.info("Accediendo a la página de visualización del formulario de registro");
        model.addAttribute("registroDto", new RegistroDto("", "", "", "", null));
        return "auth/registro";
    }
    
    /**
     * Procesa la solicitud de registro de un nuevo usuario en el sistema con validaciones incluidas.
     */
    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute RegistroDto registroDto, 
                                   BindingResult bindingResult, 
                                   Model model) {
        
        log.info("Recibida petición POST para registrar al usuario: '{}'", registroDto.username());

        // validación manual de contraseñas coincidentes
        if (!registroDto.password().equals(registroDto.confirmPassword())) {
            log.warn("Error en el formulario: Las contraseñas introducidas para el usuario '{}' no coinciden", registroDto.username());
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Las contraseñas no coinciden");
        }

        // control de fallos de validación 
        if (bindingResult.hasErrors()) {
            log.warn("El formulario de registro contiene {} error(es) de validación. Recargando vista", bindingResult.getErrorCount());
            return "auth/registro";
        }

        // intento de registro invocando al servicio
        try {
            authService.registrarNuevoUsuario(registroDto);
            log.info("Registro completado con éxito para '{}'. Redirigiendo a la pantalla de login", registroDto.username());
            return "redirect:/login?registroExitoso";
        } catch (IllegalArgumentException e) {
            log.warn("Error controlado durante el registro de '{}': {}", registroDto.username(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "auth/registro";
        }
    }
    
    /**
     * Muestra el formulario para solicitar la recuperación de contraseña.
     */
    @GetMapping("/recuperar-password")
    public String mostrarFormularioSolicitud(Model model) {
        log.info("Accediendo a la página de solicitud de recuperación de contraseña");
        model.addAttribute("solicitarResetDto", new SolicitarResetDto(""));
        return "auth/recuperar-password";
    }
    
    /**
     * Procesa la solicitud de recuperación: genera el token y envía el email si el usuario existe.
     */
    @PostMapping("/recuperar-password")
    public String procesarSolicitud(@Valid @ModelAttribute SolicitarResetDto dto,
                                      BindingResult bindingResult, Model model) {

        log.info("Recibida solicitud de recuperación de contraseña para el email: '{}'", dto.email());

        if (bindingResult.hasErrors()) {
            log.warn("El formulario de recuperación contiene {} error(es) de validación", bindingResult.getErrorCount());
            return "auth/recuperar-password";
        }

        passwordResetService.solicitarRecuperacion(dto);
        log.info("Procesada solicitud de recuperación para '{}' (no se revela si el email existe)", dto.email());
        model.addAttribute("mensaje", "Si el email existe, te hemos enviado un enlace de recuperación.");
        return "auth/recuperar-password";
    }

    /**
     * Muestra el formulario para establecer una nueva contraseña, recogiendo el token de la URL.
     */
    @GetMapping("/reset-password")
    public String mostrarFormularioReset(@RequestParam String token, Model model) {
        log.info("Accediendo a la página de reset de contraseña con un token");
        model.addAttribute("nuevaPasswordDto", new NuevaPasswordDto(token, "", ""));
        return "auth/reset-password";
    }
    
    
    /**
     * Procesa el cambio definitivo de contraseña tras validar el token.
     */
    @PostMapping("/reset-password")
    public String procesarReset(@Valid @ModelAttribute NuevaPasswordDto dto,
                                  BindingResult bindingResult, Model model) {

        log.info("Recibida petición POST para restablecer contraseña");

        // Comprobar coincidencia de contraseñas en el formulario
        if (!dto.password().equals(dto.confirmPassword())) {
            log.warn("Error en el formulario de reset: Las contraseñas no coinciden");
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Las contraseñas no coinciden");
        }
        
        if (bindingResult.hasErrors()) {
            log.warn("El formulario de nueva contraseña contiene {} error(es) de validación", bindingResult.getErrorCount());
            return "auth/reset-password";
        }

        try {
            passwordResetService.cambiarPassword(dto);
            log.info("Contraseña restablecida con éxito. Redirigiendo a la pantalla de login");
            return "redirect:/login?resetExitoso";
        } catch (IllegalArgumentException e) {
            log.warn("Error controlado durante el reset de contraseña: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("nuevaPasswordDto", dto);
            return "auth/reset-password";
        }
    }
}