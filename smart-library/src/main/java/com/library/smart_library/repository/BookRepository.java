package com.library.smart_library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.library.smart_library.model.Book;

import jakarta.persistence.LockModeType;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Yazar ismine göre arama (Büyük/küçük harf fark etmez)
    List<Book> findByAuthorContainingIgnoreCase(String author);

    // Kitap ismine göre arama
    List<Book> findByTitleContainingIgnoreCase(String title);

    // ISBN'e göre bulma
    Book findByIsbn(String isbn);

    // Kategoriye göre kitap ara
    List<Book> findByCategory(String category);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);
}