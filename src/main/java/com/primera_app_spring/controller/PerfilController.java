package com.primera_app_spring.controller;

import com.primera_app_spring.dto.EditarPerfilDto;
import com.primera_app_spring.model.User;
import com.primera_app_spring.security.CustomUserDetails;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
public class PerfilController {

    private static final Logger log = LoggerFactory.getLogger(PerfilController.class);

    private final UserService userService;

    public PerfilController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/perfil")
    public String verPerfil(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        log.info("Accediendo a la vista de perfil para el usuario: '{}'", userDetails.getUsername());
        
        // Le pasamos al modelo directamente el objeto cargado desde la sesión
        model.addAttribute("user", userDetails);
        return "usuario/perfil";
    }
    
    
    @GetMapping("/perfil/editar")
    public String mostrarFormularioEdicion(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        
    	log.info("Accediendo al formulario de edición de perfil para: '{}'", userDetails.getUsername());

        EditarPerfilDto usuarioDto = new EditarPerfilDto(userDetails.getUsername(), userDetails.getEmail(), "", "", null);
        model.addAttribute("usuarioDto", usuarioDto);

        return "usuario/editar-perfil";
    }
    
           
    @PostMapping("/perfil/editar")
    public String procesarEdicion(@AuthenticationPrincipal CustomUserDetails userDetailsActual,
                                  @Valid @ModelAttribute("usuarioDto") EditarPerfilDto usuarioDto,
                                  BindingResult bindingResult,  
                                  Model model,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        
        log.info("Procesando actualización de perfil para el usuario: '{}'", userDetailsActual.getUsername());
            
        
        // validaciones de contraseñas 
        if (!usuarioDto.password().isEmpty()) {
            if (!usuarioDto.password().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")) {
                bindingResult.rejectValue("password", "perfil.password.pattern", 
                    "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número");
            }
            if (!usuarioDto.password().equals(usuarioDto.confirmPassword())) {
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Las contraseñas no coinciden");
            }
        }

        if (bindingResult.hasErrors()) {
            return "usuario/editar-perfil";
        }

        try {
            // Actualizamos db
            User actualizado = userService.actualizarPerfil(userDetailsActual.getUsername(), usuarioDto);
            CustomUserDetails nuevoUserDetails = new CustomUserDetails(actualizado);

            // actualizamos e contexto
            var auth = new UsernamePasswordAuthenticationToken(
                    nuevoUserDetails, 
                    null, 
                    nuevoUserDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            new HttpSessionSecurityContextRepository()
                    .saveContext(SecurityContextHolder.getContext(), request, response);

            log.info("Perfil y sesión de Redis actualizados con éxito para '{}'", actualizado.getUsername());
            return "redirect:/perfil?actualizado";
            
        } catch (RuntimeException e) {
            log.warn("Error al actualizar perfil: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "usuario/editar-perfil";
        }
    }
}





