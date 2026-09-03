package com.primera_app_spring.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "usuarios") 
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "foto")
    private String foto;

    // Relación para los roles (ADMIN, USER, etc.) 
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "role")
    private Set<String> roles;

    // --- CONSTRUCTORES ---
    public User() {}

    public User(String username, String password, String email, String foto,  Set<String> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.foto = foto;
        this.roles = roles;
    }

    // --- GETTERS Y SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", foto='" + foto + '\'' +
                ", roles=" + roles +
                '}';
    }
}