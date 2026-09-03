package com.primera_app_spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Permitimos que todo el mundo acceda a los WebJars, imágenes y CSS estáticos
                .requestMatchers("/", "/index", "/webjars/**", "/css/**", "/images/**", "/registro", "/recuperar-password", "/reset-password").permitAll()
                // Cualquier otra petición requerirá autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")// El endpoint POST donde enviará los datos el formulario                 
                .loginProcessingUrl("/login")               
                .defaultSuccessUrl("/", true) // Dónde redirigir si el login es correcto          
                .permitAll()// Permitimos a todo el mundo acceder a la URL de login
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}