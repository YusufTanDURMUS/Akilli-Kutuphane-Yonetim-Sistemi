package com.library.smart_library.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.smart_library.model.User;
import com.library.smart_library.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Mevcut kullanıcıyı getir
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    // Tüm kullanıcıları getir (Sadece ADMIN)
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        User currentUser = getCurrentUser();
        if (currentUser == null || !currentUser.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("HATA: Sadece adminler bu işlemi yapabilir!");
        }

        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // Profil bilgileri getir
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("HATA: Giriş yapmalısınız!");
        }

        return ResponseEntity.ok(currentUser);
    }

    // Kullanıcı sil - Kendi hesabını veya Admin tüm hesapları silebilir
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("HATA: Giriş yapmalısınız!");
        }

        Optional<User> userToDelete = userRepository.findById(userId);
        if (userToDelete.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("HATA: Kullanıcı bulunamadı!");
        }

        User user = userToDelete.get();

        // Sadece kendi hesabını veya admin tüm hesapları silebilir
        boolean isOwnAccount = currentUser.getId().equals(userId);
        boolean isAdmin = currentUser.getRole().equals(User.Role.ADMIN);

        if (!isOwnAccount && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("HATA: Sadece kendi hesabınızı silebilirsiniz!");
        }

        userRepository.deleteById(userId);
        return ResponseEntity.ok("Hesap başarıyla silindi!");
    }

    // Kendi hesabı sil (Kısayol)
    @DeleteMapping("/delete-account")
    public ResponseEntity<String> deleteOwnAccount() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("HATA: Giriş yapmalısınız!");
        }

        userRepository.deleteById(currentUser.getId());
        return ResponseEntity.ok("Hesabınız silinmiştir!");
    }
}
