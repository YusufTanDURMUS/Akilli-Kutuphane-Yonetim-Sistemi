package com.library.smart_library.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.library.smart_library.model.Book;
import com.library.smart_library.repository.BookRepository;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    // Tüm kitapları getir
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Kitap Ekle (Artık çok basit, direkt kaydediyoruz)
    public Book addBook(Book book) {
        // Eğer kategori boş ise varsayılan "Diğer" kategorisi ata
        if (book.getCategory() == null || book.getCategory().trim().isEmpty()) {
            book.setCategory("Diğer");
        }
        // Aynı ISBN ile tekrar eklemeyi engelle (ISBN null değilse)
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            String isbn = book.getIsbn().trim();
            if (bookRepository.existsByIsbn(isbn)) {
                // Mevcut kaydı döndürerek duplicate oluşmasını engelle
                Book existing = bookRepository.findByIsbn(isbn);
                return existing != null ? existing : bookRepository.save(book);
            }
            book.setIsbn(isbn);
        }
        return bookRepository.save(book);
    }

    // Yazara göre ara
    public List<Book> searchByAuthor(String authorName) {
        return bookRepository.findByAuthorContainingIgnoreCase(authorName);
    }

    // Başlığa göre ara
    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    // ID ile bul
    public Optional<Book> getBookById(long id) {
        return bookRepository.findById(id);
    }

    // Güncelle
    public Book updateBook(long id, Book newDetails) {
        return bookRepository.findById(id).map(existingBook -> {
            existingBook.setTitle(newDetails.getTitle());
            existingBook.setAuthor(newDetails.getAuthor());
            existingBook.setIsbn(newDetails.getIsbn());
            existingBook.setCategory(newDetails.getCategory());
            existingBook.setPageCount(newDetails.getPageCount());
            existingBook.setImageUrl(newDetails.getImageUrl());
            existingBook.setStock(newDetails.getStock());
            return bookRepository.save(existingBook);
        }).orElse(null);
    }

    // Sil
    public boolean deleteBook(long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Kategoriye göre kitap sayısını getir
    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    // Tüm kategorileri ve her kategorideki kitap sayısını getir
    public java.util.Map<String, Integer> getBookCountByCategory() {
        List<Book> allBooks = bookRepository.findAll();
        java.util.Map<String, Integer> categoryCount = new java.util.LinkedHashMap<>();

        for (Book book : allBooks) {
            String category = book.getCategory() != null ? book.getCategory() : "Diğer";
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }

        return categoryCount;
    }

    // Kategoriye göre toplam stok sayısını getir
    public java.util.Map<String, Integer> getStockCountByCategory() {
        List<Book> allBooks = bookRepository.findAll();
        java.util.Map<String, Integer> categoryStock = new java.util.LinkedHashMap<>();

        for (Book book : allBooks) {
            String category = book.getCategory() != null ? book.getCategory() : "Diğer";
            categoryStock.put(category, categoryStock.getOrDefault(category, 0) + book.getStock());
        }

        return categoryStock;
    }

    // Toplam kitap stoğunu getir (tüm kitapların stock'ları topla)
    public int getTotalStock() {
        List<Book> allBooks = bookRepository.findAll();
        return allBooks.stream().mapToInt(Book::getStock).sum();
    }
}