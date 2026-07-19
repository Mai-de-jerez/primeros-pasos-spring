package com.primera_app_spring.controller;

import com.primera_app_spring.dto.RegistroDto;
import com.primera_app_spring.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        // Inicializa MockMvc manualmente para este controlador sin necesidad de levantar anotaciones pesadas
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void cuandoPidoFormularioRegistro_entoncesDevuelveVistaRegistroConDto() throws Exception {
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(model().attributeExists("registroDto"));
    }

    @Test
    void cuandoProcesarRegistroExitoso_entoncesRedirigeALoginConParametroExito() throws Exception {
        Mockito.doNothing().when(authService).registrarNuevoUsuario(any(RegistroDto.class));

        mockMvc.perform(post("/registro")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "MayLucena")
                .param("email", "may@email.com")
                .param("password", "Password123")
                .param("confirmPassword", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registroExitoso"));
    }

    @Test
    void cuandoContrasenasNoCoinciden_entoncesVuelveAFormularioConErrores() throws Exception {
        mockMvc.perform(post("/registro")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "MayLucena")
                .param("email", "may@email.com")
                .param("password", "Password123")
                .param("confirmPassword", "Diferente123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("registroDto", "confirmPassword"));
    }

    @Test
    void cuandoServicioLanzaErrorDeDuplicado_entoncesVuelveAFormularioConMensajeDeError() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Ese email ya está registrado"))
                .when(authService).registrarNuevoUsuario(any(RegistroDto.class));

        mockMvc.perform(post("/registro")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "MayLucena")
                .param("email", "may@email.com")
                .param("password", "Password123")
                .param("confirmPassword", "Password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Ese email ya está registrado"));
    }
}