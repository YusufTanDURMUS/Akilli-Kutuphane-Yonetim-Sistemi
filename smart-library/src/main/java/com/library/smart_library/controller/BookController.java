package com.library.smart_library.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.smart_library.model.Book;
import com.library.smart_library.model.User;
import com.library.smart_library.repository.UserRepository;
import com.library.smart_library.service.BookService;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    @Autowired
    private BookService bookService;

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

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam(required = false) String author,
            @RequestParam(required = false) String title) {
        if (author != null)
            return bookService.searchByAuthor(author);
        else if (title != null)
            return bookService.searchByTitle(title);
        else
            return bookService.getAllBooks();
    }

    @PostMapping
    public Book createBook(@RequestBody Book book) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            book.setCreatedBy(currentUser.getEmail());
        }

        // Resim URL'i boş veya placeholder ise, varsayılan placeholder kullan
        if (book.getImageUrl() == null || book.getImageUrl().isEmpty() || !book.getImageUrl().startsWith("http")) {
            book.setImageUrl("https://via.placeholder.com/150x240?text="
                    + book.getTitle().substring(0, Math.min(book.getTitle().length(), 20)));
        }

        return bookService.addBook(book);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.getBookById(id);
        return book.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book newDetails) {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        Optional<Book> existingBook = bookService.getBookById(id);
        if (existingBook.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Book book = existingBook.get();

        // Admin veya kitabı ekleyen kişi düzenleme yapabilir
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
        boolean isCreator = book.getCreatedBy() != null && book.getCreatedBy().equals(currentUser.getEmail());

        if (!isAdmin && !isCreator) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Book updated = bookService.updateBook(id, newDetails);
        return (updated != null) ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            return ResponseEntity.status(401).body("Giriş yapmanız gerekiyor!");
        }

        Optional<Book> existingBook = bookService.getBookById(id);
        if (existingBook.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Book book = existingBook.get();

        // Admin tüm kitapları silebilir, creator da kendi kitaplarını silebilir
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
        boolean isCreator = book.getCreatedBy() != null && book.getCreatedBy().equals(currentUser.getEmail());

        if (!isAdmin && !isCreator) {
            return ResponseEntity.status(403).body("Sadece admin veya kitabı ekleyen kişi silebilir!");
        }

        boolean deleted = bookService.deleteBook(id);
        return deleted ? ResponseEntity.ok("Kitap başarıyla silindi!") : ResponseEntity.notFound().build();
    }

    @GetMapping("/stats/categories")
    public ResponseEntity<java.util.Map<String, Integer>> getBookCountByCategory() {
        return ResponseEntity.ok(bookService.getBookCountByCategory());
    }

    @GetMapping("/stats/total-stock")
    public ResponseEntity<java.util.Map<String, Object>> getTotalStats() {
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalBooks", bookService.getAllBooks().size());
        stats.put("totalStock", bookService.getTotalStock());
        stats.put("byCategory", bookService.getBookCountByCategory());
        stats.put("stockByCategory", bookService.getStockCountByCategory());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/category/{category}")
    public List<Book> getBooksByCategory(@PathVariable String category) {
        return bookService.getBooksByCategory(category);
    }
}