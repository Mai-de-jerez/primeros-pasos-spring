package com.primera_app_spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.primera_app_spring.dto.RegistroDto;
import com.primera_app_spring.services.AuthService;

import jakarta.validation.Valid; 

@Controller
public class AuthController {
	
	private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    
    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("registroDto", new RegistroDto("", "", "", ""));
        return "auth/registro";
    }
    
    
    
    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute RegistroDto registroDto, 
                                     BindingResult bindingResult, 
                                     Model model) {

        if (!registroDto.password().equals(registroDto.confirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Las contraseñas no coinciden");
        }

        if (bindingResult.hasErrors()) {
            return "auth/registro";
        }

        try {
            authService.registrarNuevoUsuario(registroDto);
            return "redirect:/login?registroExitoso";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/registro";
        }
    }
}