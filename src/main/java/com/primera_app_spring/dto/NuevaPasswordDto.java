package com.primera_app_spring.dto;

import jakarta.validation.constraints.*;

public record NuevaPasswordDto(
        String token,

        @NotBlank(message = "{registro.password.notblank}")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$", message = "{registro.password.pattern}")
        String password,

        @NotBlank(message = "{registro.confirmPassword.notblank}")
        String confirmPassword
) {}