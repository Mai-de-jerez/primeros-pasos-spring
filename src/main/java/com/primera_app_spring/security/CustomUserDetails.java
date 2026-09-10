package com.primera_app_spring.security;

import com.primera_app_spring.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String password;
    private String email;
    private String foto;
    private Collection<? extends GrantedAuthority> authorities;

    // 1. Constructor vacío obligatorio para que Redis pueda deserializarlo
    public CustomUserDetails() {}

    // 2. Constructor que copia los datos desde tu Entidad JPA 
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.foto = user.getFoto();
        this.authorities = user.getRoles().stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // --- Métodos obligatorios de la interfaz UserDetails ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- Getters propios para acceder a los campos extra desde Thymeleaf o Controladores ---
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFoto() {
        return foto;
    }
}