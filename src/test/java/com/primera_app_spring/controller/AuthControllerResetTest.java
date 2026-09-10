package com.primera_app_spring.controller;

import com.primera_app_spring.services.AuthService;
import com.primera_app_spring.services.PasswordResetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) 
class AuthControllerResetTest {

    @Autowired
    private MockMvc mockMvc; 

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private AuthService authService; // Requerido por el constructor de AuthController

    @Test
    @DisplayName("GET /reset-password - Muestra la vista del formulario con el token cargado en el DTO")
    void testMostrarFormularioReset() throws Exception {
        mockMvc.perform(get("/reset-password").param("token", "token-de-prueba"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"))
                .andExpect(model().attributeExists("nuevaPasswordDto"));
    }

    @Test
    @DisplayName("POST /reset-password - Redirige a /login cuando el proceso es exitoso")
    void testProcesarResetExito() throws Exception {
        doNothing().when(passwordResetService).cambiarPassword(any());

        mockMvc.perform(post("/reset-password")
                        .with(csrf())
                        .param("token", "token-de-prueba")
                        .param("password", "Password123")
                        .param("confirmPassword", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?resetExitoso"));

        verify(passwordResetService, times(1)).cambiarPassword(any());
    }

    @Test
    @DisplayName("POST /reset-password - Retorna vista si las contraseñas no coinciden")
    void testProcesarResetContrasenasNoCoinciden() throws Exception {
        mockMvc.perform(post("/reset-password")
                        .with(csrf())
                        .param("token", "token-de-prueba")
                        .param("password", "Password123")
                        .param("confirmPassword", "PasswordDiferente123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"))
                .andExpect(model().hasErrors());

        verify(passwordResetService, never()).cambiarPassword(any());
    }
}