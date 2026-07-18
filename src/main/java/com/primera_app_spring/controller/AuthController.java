package com.primera_app_spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import com.primera_app_spring.dto.RegistroDto;
import com.primera_app_spring.services.AuthService; 

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
    public String procesarRegistro(@ModelAttribute RegistroDto registroDto, Model model) {
        try {
            authService.registrarNuevoUsuario(registroDto);
            return "redirect:/login?registroExitoso";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registroDto", registroDto);
            return "auth/registro";
        }
    }
}