package com.library.smart_library.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Kitap adı boş olamaz")
    private String title;

    @NotBlank(message = "Yazar adı boş olamaz")
    private String author;

    private String isbn;

    private String category; // Türü
    private int pageCount; // Sayfa Sayısı

    @Column(length = 1000)
    private String imageUrl; // Kapak Resmi

    // 👇 İŞTE BU EKSİKTİ! 👇
    private int stock = 5; // Varsayılan stok sayısı

    private String createdBy; // Kitabı kimin eklediğini tutuyor (email veya ID)

    public Book() {
    }

    public Book(String title, String author, String isbn, String category, int pageCount, String imageUrl) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.pageCount = pageCount;
        this.imageUrl = imageUrl;
        this.stock = 5; // Her yeni kitap 5 stokla başlar
    }

    // --- GETTER & SETTER ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // 👇 BUNLARI EKLEMEZSEN SERVİS HATALI OLUR 👇
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}