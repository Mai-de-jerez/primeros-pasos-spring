package com.primera_app_spring.dto;

public record RegistroDto(
        String username,
        String email,
        String password,
        String confirmPassword
) {}