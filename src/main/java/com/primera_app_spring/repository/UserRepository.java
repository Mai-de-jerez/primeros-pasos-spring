package com.primera_app_spring.repository;


import com.primera_app_spring.model.User;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    // Métodos mágicos de Spring Data para buscar en el registro
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
    int updatePasswordByEmail(@Param("email") String email, @Param("password") String password);
}