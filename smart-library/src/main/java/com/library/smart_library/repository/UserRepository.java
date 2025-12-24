package com.library.smart_library.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library.smart_library.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Email ile kullanıcı bulma (Login için şart)
    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByResetToken(String resetToken);
}