package com.primera_app_spring.controller;

import com.primera_app_spring.dto.EditarPerfilDto;
import com.primera_app_spring.model.User;
import com.primera_app_spring.security.CustomUserDetails;
import com.primera_app_spring.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PerfilControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        User usuarioActual = new User("mai", "passwordCifrada", "mai@ejemplo.com", null, Set.of("USER"));
        CustomUserDetails userDetailsMock = new CustomUserDetails(usuarioActual);

        PerfilController controller = new PerfilController(userService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver(userDetailsMock))
                .build();
    }

    @Test
    @DisplayName("GET /perfil - Debe mostrar la vista de perfil con los datos del usuario autenticado")
    void testVerPerfil() throws Exception {
        mockMvc.perform(get("/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario/perfil"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("GET /perfil/editar - Debe mostrar el formulario de edición con los datos actuales")
    void testMostrarFormularioEdicion() throws Exception {
        mockMvc.perform(get("/perfil/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario/editar-perfil"))
                .andExpect(model().attributeExists("usuarioDto"));
    }

    @Test
    @DisplayName("POST /perfil/editar - Debe redirigir a /perfil si la actualización es correcta")
    void testProcesarEdicionExito() throws Exception {
        User actualizado = new User("mai", "passwordCifrada", "nuevo@ejemplo.com", null, Set.of("USER"));
        when(userService.actualizarPerfil(eq("mai"), any(EditarPerfilDto.class))).thenReturn(actualizado);

        mockMvc.perform(post("/perfil/editar")
                        .param("username", "mai")
                        .param("email", "nuevo@ejemplo.com")
                        .param("password", "")
                        .param("confirmPassword", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil?actualizado"));

        verify(userService, times(1)).actualizarPerfil(eq("mai"), any(EditarPerfilDto.class));
    }

    @Test
    @DisplayName("POST /perfil/editar - Debe recargar el formulario si las contraseñas no coinciden")
    void testProcesarEdicionPasswordsNoCoinciden() throws Exception {
        mockMvc.perform(post("/perfil/editar")
                        .param("username", "mai")
                        .param("email", "mai@ejemplo.com")
                        .param("password", "NuevaClave123")
                        .param("confirmPassword", "OtraClave123"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario/editar-perfil"));

        verify(userService, never()).actualizarPerfil(anyString(), any());
    }

    @Test
    @DisplayName("POST /perfil/editar - Debe recargar el formulario si el servicio lanza una excepción")
    void testProcesarEdicionErrorServicio() throws Exception {
        when(userService.actualizarPerfil(eq("mai"), any(EditarPerfilDto.class)))
                .thenThrow(new RuntimeException("El email ya está en uso"));

        mockMvc.perform(post("/perfil/editar")
                        .param("username", "mai")
                        .param("email", "otro@ejemplo.com")
                        .param("password", "")
                        .param("confirmPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario/editar-perfil"))
                .andExpect(model().attributeExists("error"));
    }

    /**
     * Resolver auxiliar para inyectar un CustomUserDetails fijo donde el controlador
     * espera @AuthenticationPrincipal, sin montar un contexto de seguridad completo.
     */
    private record AuthenticationPrincipalArgumentResolver(CustomUserDetails userDetails)
            implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(CustomUserDetails.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return userDetails;
        }
    }
}