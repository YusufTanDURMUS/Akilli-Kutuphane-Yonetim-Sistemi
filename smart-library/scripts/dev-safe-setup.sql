-- DEV SAFE SETUP (Non-destructive)
-- Bu dosya veritabanını KESİNLİKLE SILMEZ. DROP DATABASE yok.
-- Amaç: library_db yoksa oluştur, tablolar yoksa yarat, örnek veri ekle.

-- 1) Veritabanı
CREATE DATABASE IF NOT EXISTS library_db 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
USE library_db;

-- 2) Tablolar
-- users (uygulamanın kullandığı alanları içerir)
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('USER','ADMIN','STUDENT') DEFAULT 'USER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reset_token VARCHAR(255) NULL UNIQUE,
  reset_token_expiry TIMESTAMP NULL,
  verified BOOLEAN DEFAULT FALSE,
  verification_token VARCHAR(255) NULL UNIQUE,
  verification_token_expiry TIMESTAMP NULL,
  INDEX idx_email (email),
  INDEX idx_reset_token (reset_token),
  INDEX idx_verification_token (verification_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- books
CREATE TABLE IF NOT EXISTS books (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(255),
  category VARCHAR(255),
  isbn VARCHAR(255),
  page_count INT,
  stock INT DEFAULT 0,
  image_url VARCHAR(1000),
  INDEX idx_title (title),
  INDEX idx_author (author),
  INDEX idx_category (category),
  INDEX idx_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- borrows
CREATE TABLE IF NOT EXISTS borrows (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  borrow_date DATE,
  due_date DATE,
  return_date DATE DEFAULT NULL,
  book_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_book_id (book_id),
  INDEX idx_return_date (return_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3) Admin varsa güncelle, yoksa ekle (email benzersiz olduğu için güvenli)
INSERT INTO users (name, email, password, role, verified)
SELECT 'admin', 'yusuftandurmus61@icloud.com', '$2a$10$DUMMYHASHREPLACEME', 'ADMIN', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'yusuftandurmus61@icloud.com');

-- 4) Örnek kitap tohum veri (idempotent ekleme: isbn eşsiz değilse tekrar eklenebilir)
INSERT INTO books (title, author, category, isbn, page_count, stock, image_url) VALUES
('Nutuk', 'Mustafa Kemal Atatürk', 'Tarih', '9789751020001', 599, 100, 'https://i.dr.com.tr/cache/600x600-0/originals/0000000064038-1.jpg'),
('Suç ve Ceza', 'Fyodor Dostoyevski', 'Dünya Klasikleri', '9789750719387', 687, 20, 'https://i.dr.com.tr/cache/600x600-0/originals/0000000064112-1.jpg'),
('Sefiller', 'Victor Hugo', 'Dünya Klasikleri', '9789750739927', 1724, 15, 'https://i.dr.com.tr/cache/600x600-0/originals/0001792758001-1.jpg'),
('Kürk Mantolu Madonna', 'Sabahattin Ali', 'Türk Edebiyatı', '9789753638029', 160, 50, 'https://i.dr.com.tr/cache/600x600-0/originals/0000000064031-1.jpg'),
('1984', 'George Orwell', 'Bilim Kurgu', '9789750718533', 352, 40, 'https://i.dr.com.tr/cache/600x600-0/originals/0000000064035-1.jpg');

-- Notlar:
-- - Bu dosya DROP yapmaz, verileri silmez.
-- - Mevcut tablo/indeks varsa aynen bırakır; yoksa oluşturur.
-- - Admin parolası dummy olarak bırakıldı; uygulama üzerinden yeniden belirleyin.
