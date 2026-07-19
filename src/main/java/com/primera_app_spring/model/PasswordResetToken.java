package com.primera_app_spring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id // Clave primaria directa sin auto-incremento (PK y NN)
    @Column(length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String token;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // --- CONSTRUCTORES ---
    public PasswordResetToken() {}

    public PasswordResetToken(String email, String token) {
        this.email = email;
        this.token = token;
        this.createdAt = LocalDateTime.now();
    }

    // --- LÓGICA DE CONTROL ---
    /**
     * Evalúa si el token ha expirado sumándole los minutos de margen al momento de creación.
     */
    public boolean isExpired(int expirationMinutes) {
        return LocalDateTime.now().isAfter(this.createdAt.plusMinutes(expirationMinutes));
    }

    // --- GETTERS Y SETTERS ---
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
