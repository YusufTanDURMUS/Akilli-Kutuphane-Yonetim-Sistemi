# Akıllı Kütüphane Yönetim Sistemi

Bu proje, Proje Tabanlı Öğrenme kapsamında geliştirilmiş, JPA Entities tabındansından yararlanılarak oluşturulan, JWT token tabanlı kimlik doğrulama ile korunan bir Spring Boot Rest API'dir.

## Projenin Amacı

- Kütüphane yönetimini dijitalleştirmek ve otomatize etmek
- Kitap envanterinin anlık takibini sağlamak
- Ödünç alma ve iade işlemlerini sistem tarafından yönetmek
- Geç iade durumlarında otomatik ceza hesaplama
- Veritabanı Triggers ve Normalizasyon (3NF) konularını uygulamak

## Kullanılan Teknolojiler

| Kategori | Teknoloji |
|----------|-----------|
| Backend | Java 21, Spring Boot 3.5.0 |
| Veritabanı | MySQL 8.0+ |
| ORM | Hibernate/JPA |
| Frontend | HTML5, CSS3, JavaScript |
| Güvenlik | Spring Security, JWT, BCrypt |
| Build Tool | Maven 3.6+ |
| API Test | Postman |

## Proje Klasör Yapısı

```
smart-library/
├── src/main/java/com/library/smart_library/
│   ├── SmartLibraryApplication.java
│   ├── auth/
│   │   ├── controller/       (Kimlik doğrulama endpoint'leri)
│   │   ├── service/          (JWT, BCrypt işlemleri)
│   │   └── filter/           (JWT doğrulama filtreleri)
│   ├── controller/           (REST API uç noktaları)
│   ├── service/              (İş mantığı katmanı)
│   ├── repository/           (JPA Repository'ler)
│   ├── model/                (Entity sınıfları)
│   ├── exception/            (Custom exceptions)
│   ├── security/             (Security konfigürasyonu)
│   └── util/                 (Yardımcı sınıflar)
│
├── src/main/resources/
│   ├── application.properties (Konfigürasyon)
│   ├── db/migration/         (Flyway migration scriptleri)
│   └── static/
│       ├── index.html
│       ├── books.html
│       ├── verify-email.html
│       ├── reset-password.html
│       ├── style.css
│       └── app.js
│
├── src/test/
│   └── java/com/library/smart_library/
│       └── (Unit testler)
│
├── pom.xml                   (Maven bağımlılıkları)
├── mvnw / mvnw.cmd          (Maven Wrapper)
├── README.md                 (Bu dosya)
├── PROJE_RAPORU.md          (Detaylı proje raporu)
└── VERITABANI_SETUP.sql     (Veritabanı kurulum scripti)
```

## Veritabanı Tasarımı

### Tablolar ve İlişkiler

**USERS** (Kullanıcı bilgileri)
- id (PK), name, email (UNIQUE)
- password (BCrypt hash), role (ENUM)
- verified, verification_token, reset_token
- created_at, updated_at

**BOOKS** (Kitap kataloğu)
- id (PK), isbn (UNIQUE), title, author
- category, page_count, stock
- image_url, created_at, updated_at

**BORROWS** (Ödünç işlemleri)
- id (PK), user_id (FK), book_id (FK)
- borrow_date, due_date, return_date
- created_at, updated_at

**PENALTIES** (Ceza kayıtları)
- id (PK), user_id (FK), borrow_id (FK)
- days_overdue, fine_amount, paid
- created_at, updated_at

**AUDIT_LOG** (İşlem izleme)
- id (PK), table_name, operation
- record_id, user_id, description, timestamp

### Tetikleyiciler (Triggers)

1. **tr_decrease_stock_on_borrow**
   - BORROWS tablosuna INSERT → BOOKS.stock azalır
   
2. **tr_increase_stock_on_return**
   - BORROWS.return_date güncellendiğinde → BOOKS.stock artar
   
3. **tr_create_penalty_on_late_return**
   - İade tarihi > vade tarihi ise → PENALTIES tablosuna ceza eklenir
   - Ceza: (geç gün sayısı) × 10 TL
   
4. **tr_audit_*** (İşlem İzleme)
   - Tüm INSERT/UPDATE/DELETE işlemleri AUDIT_LOG'a kaydedilir


## API Endpoints

### Kimlik Doğrulama
- `POST /api/auth/register` - Yeni kullanıcı kaydı
- `POST /api/auth/login` - Sisteme giriş (JWT token döner)
- `POST /api/auth/forgot-password` - Şifre sıfırlama isteği
- `GET /api/auth/verify-email` - E-posta doğrulama

### Kitaplar
- `GET /api/books` - Tüm kitapları listele
- `GET /api/books/{id}` - Kitap detaylarını görüntüle
- `POST /api/books` - Yeni kitap ekle (Admin)
- `PUT /api/books/{id}` - Kitap bilgilerini güncelle (Admin)
- `DELETE /api/books/{id}` - Kitapı sil (Admin)

### Ödünç İşlemleri
- `POST /api/borrow` - Kitap ödünç al
- `PUT /api/return/{borrowId}` - Kitap iade et
- `GET /api/my-books` - Ödünçlerinizi görebileceğiniz işlemler

### Admin İşlemleri
- `GET /api/admin/users` - Tüm kullanıcıları listele
- `GET /api/admin/statistics` - Sistem istatistikleri


## Güvenlik Özellikleri

- **Şifre Güvenliği:** BCrypt ile hashing
- **JWT Token:** 24 saat geçerlilik
- **Rol Tabanlı Erişim:** Admin/User ayrımı
- **SQL Injection Koruması:** Parametreli sorgular
- **CORS:** Uygun originlerin konfigürasyonu
- **E-posta Doğrulama:** Hesap aktivasyonunda gerekli

## Veritabanı Normalizasyon

Proje **3. Normal Form (3NF)** kurallarına uygun tasarlanmıştır:

- **1NF:** Tüm alanlar atomik, tekrarlayan grup yok
- **2NF:** Partial dependency kaldırıldı
- **3NF:** Transitive dependency kaldırıldı

## Yazarı

**Yusuf Tan Durmuş** - 445841  
**Dersi Veren:** Hakan Aydın  
**Ders:** YZM2017 - Veri Tabanı ve Yönetimi  
**Teslim Tarihi:** 24 Aralık 2025

---

Detaylı teknik bilgi için **PROJE_RAPORU.md** dosyasını inceleyiniz.
