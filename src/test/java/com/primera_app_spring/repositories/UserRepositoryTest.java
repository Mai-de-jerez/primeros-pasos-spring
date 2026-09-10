package com.primera_app_spring.repositories;

import com.primera_app_spring.model.User;
import com.primera_app_spring.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Debe actualizar la contraseña por email directamente en base de datos")
    void updatePasswordByEmail_Exito() {
        // Arrange 
        User usuario = new User();
        usuario.setUsername("may_test");
        usuario.setEmail("may@test.com");
        usuario.setPassword("pass_antigua");
        userRepository.save(usuario);

        // Act
        int filasAfectadas = userRepository.updatePasswordByEmail("may@test.com", "pass_nueva");
        Optional<User> usuarioActualizado = userRepository.findByEmail("may@test.com");

        // Assert
        assertThat(filasAfectadas).isEqualTo(1);
        assertThat(usuarioActualizado).isPresent();
        assertThat(usuarioActualizado.get().getPassword()).isEqualTo("pass_nueva");
    }
}