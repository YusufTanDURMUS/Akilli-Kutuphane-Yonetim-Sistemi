package com.library.smart_library.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.smart_library.model.User;
import com.library.smart_library.repository.UserRepository;
import com.library.smart_library.security.JwtUtil;
import com.library.smart_library.service.EmailService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    // --- KAYIT OL ---
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest req) {
        // 1. E-posta kontrolü
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("HATA: Bu e-posta adresi zaten kayıtlı!");
        }

        // 2. Yeni User oluştur ve ŞİFREYİ ŞİFRELE
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        // 👇 ŞİFRELEME BURADA YAPILIYOR (Çok Önemli)
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        // Rol kontrolü (Gelmese bile USER yap)
        if (req.getRole() != null) {
            user.setRole(req.getRole());
        } else {
            user.setRole(User.Role.USER);
        }

        // 3. Kullanıcıyı otomatik doğrulanmış olarak kaydet
        user.setVerified(true);
        User savedUser = userRepository.save(user);

        // Hoş geldin maili gönder
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());
        } catch (Exception e) {
            System.err.println("Hoş geldin maili gönderilemedi: " + e.getMessage());
        }

        // Token üret ve direkt döndür
        String token = jwtUtil.generateTokenWithRole(user.getEmail(), user.getRole().toString());
        return ResponseEntity.ok(token);
    }

    // --- GİRİŞ YAP ---
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Önce kullanıcıyı bul
            Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("HATA: Email veya Şifre Yanlış!");
            }

            User user = userOpt.get();

            // E-posta doğrulaması kontrolü - Authentication'dan önce
            if (!user.isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("HATA: E-posta adresiniz doğrulanmamış! Lütfen e-postanızdaki doğrulama linkine tıklayın.");
            }

            // Spring Security ile otomatik doğrulama
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            // Kullanıcının rolünü al ve token'a ekle
            String userRole = user.getRole().toString();
            String token = jwtUtil.generateTokenWithRole(loginRequest.getEmail(), userRole);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            // Şifre yanlışsa 401 hatası dön
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("HATA: Email veya Şifre Yanlış!");
        } catch (Exception e) {
            // Başka bir hata varsa
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("HATA: Sunucu sorunu -> " + e.getMessage());
        }
    }

    // --- E-POSTA DOĞRULAMA ---
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Geçersiz doğrulama bağlantısı.");
        }

        User user = userOpt.get();
        if (user.getVerificationTokenExpiry() == null
                || LocalDateTime.now().isAfter(user.getVerificationTokenExpiry())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Doğrulama bağlantısının süresi dolmuş.");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
        return ResponseEntity.ok("E-posta doğrulandı! Artık giriş yapabilirsin.");
    }

    // --- KULLANICILARI LİSTELE (Test İçin) ---
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // --- ŞİFREMİ UNUTTUM - TOKEN GÖNDER ---
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("HATA: Bu e-posta adresi kayıtlı değil!");
        }

        User user = userOpt.get();

        // Sıfırlama token'ı oluştur (UUID)
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);

        // Token'ın geçerlilik süresi: 24 saat
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // Reset linki oluştur (statik sayfa)
        String resetLink = "http://localhost:8082/reset-password.html?token=" + resetToken;

        // Email gönder
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Mail gönderilemedi: " + e.getMessage());
        }

        return ResponseEntity.ok("Şifre sıfırlama linki e-posta adresinize gönderildi!");
    }

    // --- ŞİFRE SIFIRLAMA - YENİ ŞİFRE AYARLA ---
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest req) {
        Optional<User> userOpt = userRepository.findByResetToken(req.getToken());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("HATA: Geçersiz veya bulunamadı token!");
        }

        User user = userOpt.get();

        // Token süresi kontrolü
        if (user.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("HATA: Token süresi dolmuş!");
        }

        // Yeni şifre ayarla
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Şifreniz başarıyla sıfırlandı!");
    }

    // --- ŞİFRE SÜRESİ KONTROL ET ---
    @GetMapping("/validate-reset-token")
    public ResponseEntity<String> validateResetToken(@RequestParam String token) {
        Optional<User> userOpt = userRepository.findByResetToken(token);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token bulunamadı!");
        }

        User user = userOpt.get();
        if (user.getResetTokenExpiry() != null && LocalDateTime.now().isBefore(user.getResetTokenExpiry())) {
            return ResponseEntity.ok("Token geçerli");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token süresi dolmuş!");
    }

    // ==========================================
    // Yardımcı Sınıflar (DTO) - Dosya İçinde
    // ==========================================

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private User.Role role;

        // Getter & Setter
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public User.Role getRole() {
            return role;
        }

        public void setRole(User.Role role) {
            this.role = role;
        }
    }

    // ŞİFREMİ UNUTTUM - İSTEK DTO'SU
    public static class ForgotPasswordRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    // ŞİFRE SIFIRLAMA - İSTEK DTO'SU
    public static class ResetPasswordRequest {
        private String email;
        private String token;
        private String newPassword;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}