package com.library.smart_library.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.smart_library.model.Book;
import com.library.smart_library.model.Borrow;
import com.library.smart_library.model.User;
import com.library.smart_library.repository.BookRepository;
import com.library.smart_library.repository.BorrowRepository;
import com.library.smart_library.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/internal")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BookRepository bookRepository;

    // Sadece admin'lerin erişebileceği metodları kontrol eden yardımcı fonksiyon
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent() && user.get().getRole() == User.Role.ADMIN;
    }

    public static class CreateAdminRequest {
        public String name;
        public String email;
        public String password;
    }

    // WARNING: This endpoint is intentionally simple for local/dev use only.
    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin(@RequestBody CreateAdminRequest req) {
        if (req == null || req.email == null || req.password == null) {
            return ResponseEntity.badRequest().body("Eksik parametre");
        }

        Optional<User> existing = userRepository.findByEmail(req.email);
        if (existing.isPresent()) {
            User u = existing.get();
            u.setRole(User.Role.ADMIN);
            userRepository.save(u);
            return ResponseEntity.ok("Varolan kullanıcı admin yapıldı.");
        }

        User u = new User(req.name == null ? "admin" : req.name, req.email,
                passwordEncoder.encode(req.password), User.Role.ADMIN);
        userRepository.save(u);
        return ResponseEntity.ok("Yeni admin oluşturuldu.");
    }

    // Tüm kullanıcıları listele (Sadece Admin)
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("Yetkiniz yok!");
        }
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // Tüm ödünç alma işlemlerini listele (Sadece Admin)
    @GetMapping("/borrows")
    public ResponseEntity<?> getAllBorrows() {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("Yetkiniz yok!");
        }
        List<Borrow> borrows = borrowRepository.findAll();
        return ResponseEntity.ok(borrows);
    }

    // Kategorisi olmayan kitapları düzelt (Sadece Admin)
    @PostMapping("/fix-categories")
    public ResponseEntity<String> fixBookCategories() {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("Yetkiniz yok!");
        }

        List<Book> allBooks = bookRepository.findAll();
        int fixedCount = 0;

        for (Book book : allBooks) {
            if (book.getCategory() == null || book.getCategory().trim().isEmpty()) {
                book.setCategory("Diğer");
                bookRepository.save(book);
                fixedCount++;
            }
        }

        return ResponseEntity.ok(fixedCount + " kitabın kategorisi 'Diğer' olarak güncellendi.");
    }

    // Kullanıcı rolünü değiştir (Sadece Admin)
    public static class UpdateRoleRequest {
        public String role; // "USER" veya "ADMIN"
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable long userId, @RequestBody UpdateRoleRequest req) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("Yetkiniz yok!");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Kullanıcı bulunamadı!");
        }

        User user = userOpt.get();

        try {
            User.Role newRole = User.Role.valueOf(req.role.toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);
            return ResponseEntity.ok("Kullanıcının rolü " + newRole + " olarak güncellendi.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Geçersiz rol! Kullanılabilir: USER, ADMIN");
        }
    }
}
