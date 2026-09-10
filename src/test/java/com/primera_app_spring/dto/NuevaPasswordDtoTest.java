package com.primera_app_spring.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NuevaPasswordDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Debe pasar la validación cuando todos los campos son válidos y la contraseña cumple el patrón")
    void testNuevaPasswordDtoValido() {
        NuevaPasswordDto dto = new NuevaPasswordDto("token123", "Password123", "Password123");

        Set<ConstraintViolation<NuevaPasswordDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "No debería haber fallos de validación con datos correctos");
    }

    @Test
    @DisplayName("Debe fallar la validación cuando la contraseña no cumple el patrón de seguridad")
    void testNuevaPasswordDtoPasswordInvalida() {
        // Clave sin mayúsculas ni números y demasiado corta
        NuevaPasswordDto dto = new NuevaPasswordDto("token123", "corta", "corta");

        Set<ConstraintViolation<NuevaPasswordDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty(), "Debería detectar que la contraseña no cumple con la complejidad requerida");
    }
}