package com.primera_app_spring.dto;

import jakarta.validation.constraints.*;

public record SolicitarResetDto(
        
		@NotBlank(message = "{reset.email.notblank}")
        @Email(regexp = ".+@.+\\..+", message = "{reset.email.invalid}")
        String email
) {}