package com.library.smart_library.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.smart_library.model.Book;
import com.library.smart_library.model.User;
import com.library.smart_library.repository.BookRepository;
import com.library.smart_library.repository.UserRepository;
import com.library.smart_library.service.EmailService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BookRepository bookRepository;

    @PersistenceContext
    private EntityManager entityManager;

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

    // 1) ISBN'e göre duplicate gruplarını getir
    @GetMapping("/books/duplicates/isbn")
    public ResponseEntity<List<Map<String, Object>>> getDuplicateIsbnGroups() {
        List<Object[]> rows = bookRepository.findDuplicateIsbnCounts();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("isbn", r[0]);
            item.put("count", r[1]);
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    // 2) ISBN duplicate temizliği (her grupta en düşük id kalır)
    @PostMapping("/books/dedup/isbn")
    public ResponseEntity<Map<String, Object>> dedupIsbn(
            @RequestParam(name = "dryRun", defaultValue = "false") boolean dryRun) {
        List<Object[]> rows = bookRepository.findDuplicateIsbnCounts();
        int deleted = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        for (Object[] r : rows) {
            String isbn = (String) r[0];
            List<Book> group = bookRepository.findAllByIsbnOrderByIdAsc(isbn);
            if (group.size() > 1) {
                Book keeper = group.get(0);
                List<Long> toDeleteIds = new ArrayList<>();
                for (int i = 1; i < group.size(); i++) {
                    toDeleteIds.add(group.get(i).getId());
                }
                if (!dryRun) {
                    for (Long id : toDeleteIds) {
                        if (id != null) {
                            bookRepository.deleteById(id);
                            deleted++;
                        }
                    }
                }
                Map<String, Object> info = new HashMap<>();
                info.put("isbn", isbn);
                info.put("keptId", keeper.getId());
                info.put("deletedIds", toDeleteIds);
                details.add(info);
            }
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("deleted", deleted);
        resp.put("groups", details);
        return ResponseEntity.ok(resp);
    }

    // 3) NULL ISBN duplicate temizliği (title+author eşleşenlerde en düşük id
    // kalır)
    @PostMapping("/books/dedup/null-isbn")
    public ResponseEntity<Map<String, Object>> dedupNullIsbn(
            @RequestParam(name = "dryRun", defaultValue = "false") boolean dryRun) {
        List<Object[]> rows = bookRepository.findDuplicateNullIsbnTitleAuthorCounts();
        int deleted = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        for (Object[] r : rows) {
            String title = (String) r[0];
            String author = (String) r[1];
            List<Book> group = bookRepository.findAllNullIsbnByTitleAuthorOrderByIdAsc(title, author);
            if (group.size() > 1) {
                Book keeper = group.get(0);
                List<Long> toDeleteIds = new ArrayList<>();
                for (int i = 1; i < group.size(); i++) {
                    toDeleteIds.add(group.get(i).getId());
                }
                if (!dryRun) {
                    for (Long id : toDeleteIds) {
                        if (id != null) {
                            bookRepository.deleteById(id);
                            deleted++;
                        }
                    }
                }
                Map<String, Object> info = new HashMap<>();
                info.put("title", title);
                info.put("author", author);
                info.put("keptId", keeper.getId());
                info.put("deletedIds", toDeleteIds);
                details.add(info);
            }
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("deleted", deleted);
        resp.put("groups", details);
        return ResponseEntity.ok(resp);
    }

    // 4) ISBN alanına benzersizlik kısıtı ekle (UNIQUE)
    @PostMapping("/books/constraint/isbn-unique")
    public ResponseEntity<String> addIsbnUniqueConstraint() {
        try {
            entityManager.createNativeQuery("ALTER TABLE books ADD CONSTRAINT uq_books_isbn UNIQUE (isbn)")
                    .executeUpdate();
            return ResponseEntity.ok("UNIQUE(uq_books_isbn) eklendi.");
        } catch (Exception e) {
            String msg = e.getMessage();
            return ResponseEntity.ok("Kısıt eklenemedi veya zaten var: " + msg);
        }
    }

    // 5) Temizlik sonrası hızlı doğrulama
    @GetMapping("/books/verify")
    public ResponseEntity<Map<String, Object>> verifyCleanup() {
        Map<String, Object> res = new HashMap<>();
        long total = bookRepository.count();
        List<Object[]> dupIsbn = bookRepository.findDuplicateIsbnCounts();
        List<Object[]> dupNull = bookRepository.findDuplicateNullIsbnTitleAuthorCounts();
        res.put("total", total);
        res.put("duplicateIsbnGroups", dupIsbn.size());
        res.put("duplicateNullIsbnGroups", dupNull.size());
        return ResponseEntity.ok(res);
    }
}
