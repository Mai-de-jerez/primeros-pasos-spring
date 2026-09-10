package com.primera_app_spring.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Test
    void medirRendimientoCargaUsuario() {
        // Asegúrate de tener un usuario creado en la base de datos de test con este username
        String usernamePrueba = "Mai";

        // 1. Tomamos el tiempo justo antes de la llamada
        long inicio = System.currentTimeMillis();

        // 2. Ejecutamos el método que llama a la BD y transforma la entidad a CustomUserDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(usernamePrueba);

        // 3. Tomamos el tiempo final
        long fin = System.currentTimeMillis();

        long tiempoTotal = fin - inicio;

        System.out.println("=========================================");
        System.out.println("Carga de usuario y roles completada en: " + tiempoTotal + " ms");
        System.out.println("=========================================");

        assertNotNull(userDetails);
    }
}
