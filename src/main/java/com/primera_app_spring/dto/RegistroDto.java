package com.primera_app_spring.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;

public record RegistroDto(

		@NotBlank(message = "{registro.username.notblank}")
        @Size(min = 3, max = 50, message = "{registro.username.size}")
        String username,
        
        @NotBlank(message = "{registro.email.notblank}")
        @Email(regexp = ".+@.+\\..+", message = "{registro.email.invalid}")
        String email,
        
        @NotBlank(message = "{registro.password.notblank}")
		@Pattern(
		        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
		        message = "{registro.password.pattern}"
		)
        String password,
        
        @NotBlank(message = "{registro.confirmPassword.notblank}")
        String confirmPassword,
        
        MultipartFile foto
) {}




