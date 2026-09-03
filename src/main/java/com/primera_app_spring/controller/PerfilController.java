package com.primera_app_spring.controller;

import com.primera_app_spring.dto.EditarPerfilDto;
import com.primera_app_spring.model.User;
import com.primera_app_spring.services.UserService;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;


import java.security.Principal;

@Controller
public class PerfilController {

    private static final Logger log = LoggerFactory.getLogger(PerfilController.class);

    private final UserService userService;

    public PerfilController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/perfil")
    public String verPerfil(Principal principal, Model model) {
        log.info("Accediendo a la vista de perfil para el usuario: '{}'", principal.getName());

        User usuario = userService.buscarPorUsername(principal.getName());

        model.addAttribute("user", usuario);
        return "usuario/perfil";
    }
    
    @GetMapping("/perfil/editar")
    public String mostrarFormularioEdicion(Principal principal, Model model) {
        log.info("Accediendo al formulario de edición de perfil para: '{}'", principal.getName());

        User usuario = userService.buscarPorUsername(principal.getName());
        
        // Precargamos el DTO con los datos actuales (contraseñas vacías)
        EditarPerfilDto usuarioDto = new EditarPerfilDto(usuario.getUsername(), usuario.getEmail(), "", "", null);
        model.addAttribute("usuarioDto", usuarioDto);
        
        return "usuario/editar-perfil";
    }
    
        
    
    @PostMapping("/perfil/editar")
    public String procesarEdicion(Principal principal,
                                  @Valid @ModelAttribute("usuarioDto") EditarPerfilDto usuarioDto,
                                  BindingResult bindingResult,  
                                  Model model,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        log.info("Procesando actualización de perfil para el usuario: '{}'", principal.getName());
            
        
        // Validación de patrón de contraseña (solo si se ha rellenado)
        if (!usuarioDto.password().isEmpty()) {
            if (!usuarioDto.password().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
                log.warn("Error en el formulario: La contraseña no cumple el patrón de seguridad para '{}'", principal.getName());
                bindingResult.rejectValue("password", "perfil.password.pattern", 
                    "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial");
            }
            
            // Validación de contraseñas coincidentes
            if (!usuarioDto.password().equals(usuarioDto.confirmPassword())) {
                log.warn("Error en el formulario: Las contraseñas no coinciden para el usuario '{}'", principal.getName());
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Las contraseñas no coinciden");
            }
        }

        // Control de fallos de validación
        if (bindingResult.hasErrors()) {
            log.warn("El formulario de edición contiene {} error(es) de validación. Recargando vista", bindingResult.getErrorCount());
            return "usuario/editar-perfil";
        }

        try {
            User actualizado = userService.actualizarPerfil(principal.getName(), usuarioDto);

            // Actualizar contexto de seguridad
            var auth = new UsernamePasswordAuthenticationToken(
                    actualizado.getUsername(), null,
                    AuthorityUtils.createAuthorityList(actualizado.getRoles().toArray(new String[0])));
            SecurityContextHolder.getContext().setAuthentication(auth);
            new HttpSessionSecurityContextRepository()
                    .saveContext(SecurityContextHolder.getContext(), request, response);

            log.info("Perfil actualizado con éxito para '{}'", principal.getName());
            return "redirect:/perfil?actualizado";
            
        } catch (IllegalArgumentException e) {
            // Capturar errores de negocio (username o email duplicado)
            log.warn("Error controlado durante la actualización de perfil de '{}': {}", principal.getName(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "usuario/editar-perfil";
            
        } catch (RuntimeException e) {
            // Capturar errores inesperados (foto, etc.)
            log.warn("Error inesperado al actualizar el perfil de '{}': {}", principal.getName(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "usuario/editar-perfil";
        }
    }
}





