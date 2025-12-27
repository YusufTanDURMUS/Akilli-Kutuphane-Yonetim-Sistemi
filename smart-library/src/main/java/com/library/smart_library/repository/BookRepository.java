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

    // ISBN var mı kontrol et (duplicate eklemeyi önlemek için)
    boolean existsByIsbn(String isbn);

    // Kategoriye göre kitap ara
    List<Book> findByCategory(String category);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    // ISBN bazlı duplicate grupları (isbn, count)
    @Query("select b.isbn as isbn, count(b.id) as cnt from Book b where b.isbn is not null group by b.isbn having count(b.id) > 1")
    List<Object[]> findDuplicateIsbnCounts();

    // Bir ISBN'e ait tüm kayıtları id artan sırada getir
    @Query("select b from Book b where b.isbn = :isbn order by b.id asc")
    List<Book> findAllByIsbnOrderByIdAsc(@Param("isbn") String isbn);

    // NULL ISBN için title+author bazlı duplicate grupları (title, author, count)
    @Query("select lower(b.title) as title, lower(b.author) as author, count(b.id) as cnt from Book b where b.isbn is null group by lower(b.title), lower(b.author) having count(b.id) > 1")
    List<Object[]> findDuplicateNullIsbnTitleAuthorCounts();

    // Belirli title+author (case-insensitive) ve NULL ISBN kayıtlarını id artan
    // sırada getir
    @Query("select b from Book b where b.isbn is null and lower(b.title) = lower(:title) and lower(b.author) = lower(:author) order by b.id asc")
    List<Book> findAllNullIsbnByTitleAuthorOrderByIdAsc(@Param("title") String title, @Param("author") String author);
}