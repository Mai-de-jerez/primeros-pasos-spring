package com.primera_app_spring.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;


public record EditarPerfilDto(
	    
	    @NotBlank(message = "{edit.username.notblank}")
	    @Size(min = 3, max = 50, message = "{edit.username.size}")
	    String username,

	    @NotBlank(message = "{edit.email.notblank}")
	    @Email(regexp = ".+@.+\\..+", message = "{edit.email.invalid}")
	    String email,
	    

	    String password,
	    
	    String confirmPassword,

	    MultipartFile foto
	) {}




















