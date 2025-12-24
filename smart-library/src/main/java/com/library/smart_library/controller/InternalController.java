package com.library.smart_library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.smart_library.model.Book;
import com.library.smart_library.model.User;
import com.library.smart_library.repository.BookRepository;
import com.library.smart_library.repository.UserRepository;
import com.library.smart_library.service.EmailService;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BookRepository bookRepository;

    // Tüm kullanıcıları doğrulanmış yap (Sadece geliştirme için!)
    @PostMapping("/verify-all-users")
    public ResponseEntity<String> verifyAllUsers() {
        List<User> users = userRepository.findAll();
        int count = 0;

        for (User user : users) {
            if (!user.isVerified()) {
                user.setVerified(true);
                userRepository.save(user);
                count++;
            }
        }

        return ResponseEntity.ok(count + " kullanıcı doğrulandı!");
    }

    // Mail gönderim testini yap
    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail() {
        try {
            emailService.sendEmail(
                    "yuyu_61_ts@hotmail.com",
                    "Test Maili - Smart Library",
                    "Bu bir test mailidir. Mail sisteminiz çalışıyor! 🎉");
            return ResponseEntity.ok("Test maili gönderildi! Gelen kutunuzu kontrol edin.");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Mail gönderilemedi: " + e.getMessage());
        }
    }

    // Kitap resimlerini düzelt
    @PostMapping("/fix-book-images")
    public ResponseEntity<String> fixBookImages() {
        List<Book> books = bookRepository.findAll();
        int count = 0;

        for (Book book : books) {
            String imageUrl = book.getImageUrl();

            // Eğer imageUrl boş, null veya sadece dosya adıysa
            if (imageUrl == null || imageUrl.isEmpty() || !imageUrl.startsWith("http")) {
                // Placeholder URL oluştur
                String title = book.getTitle().substring(0, Math.min(book.getTitle().length(), 20));
                book.setImageUrl("https://via.placeholder.com/150x240?text=" + title);
                bookRepository.save(book);
                count++;
            }
        }

        return ResponseEntity.ok(count + " kitabın resmi düzeltildi!");
    }

    // Boş kategorileri "Diğer" olarak düzelt
    @PostMapping("/fix-empty-categories")
    public ResponseEntity<String> fixEmptyCategories() {
        List<Book> books = bookRepository.findAll();
        int count = 0;

        for (Book book : books) {
            String category = book.getCategory();

            // Eğer kategori null veya boşsa "Diğer" yap
            if (category == null || category.isEmpty()) {
                book.setCategory("Diğer");
                bookRepository.save(book);
                count++;
            }
        }

        return ResponseEntity.ok(count + " kitabın kategorisi \"Diğer\" olarak güncellendi!");
    }
}
