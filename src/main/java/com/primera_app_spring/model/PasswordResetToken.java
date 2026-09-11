package com.primera_app_spring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(nullable = false, length = 255)
    private String token;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // --- CONSTRUCTORES ---
    
    public PasswordResetToken() {}

    public PasswordResetToken(User usuario, String token) {
        this.usuario = usuario;
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
