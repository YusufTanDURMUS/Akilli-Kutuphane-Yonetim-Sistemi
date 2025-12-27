# VERITABANI KURULUM VE TETİKLEYİCİ SCRIPTLERI

## İçindekiler
1. [Veritabanı Oluşturma](#veritabanı-oluşturma)
2. [Tablo Oluşturma](#tablo-oluşturma)
3. [Tetikleyicilerin Kurulumu](#tetikleyicilerin-kurulumu)
4. [Test Verisi Yükleme](#test-verisi-yükleme)
5. [Veritabanı Bakımı](#veritabanı-bakımı)

---

## Veritabanı Oluşturma

### 1. Veritabanı Oluştur

```sql
-- Tüm yeni veritabanı oluştur (eğer yoksa)
CREATE DATABASE IF NOT EXISTS library_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Oluşturulan veritabanını seç
USE library_db;

-- Veritabanı bilgilerini göster
SHOW VARIABLES LIKE 'character_set_database';
SHOW VARIABLES LIKE 'collation_database';
```

**Çıktı Örneği:**
```
character_set_database: utf8mb4
collation_database: utf8mb4_unicode_ci
```

---

## Tablo Oluşturma

### 1. Users Tablosu

```sql
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('USER', 'ADMIN', 'STUDENT') DEFAULT 'USER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reset_token VARCHAR(255) NULL UNIQUE,
  reset_token_expiry TIMESTAMP NULL,
  verified BOOLEAN DEFAULT FALSE,
  verification_token VARCHAR(255) NULL UNIQUE,
  verification_token_expiry TIMESTAMP NULL,
  
  INDEX idx_email (email),
  INDEX idx_reset_token (reset_token),
  INDEX idx_verification_token (verification_token),
  
  CONSTRAINT chk_email FORMAT CHECK (email LIKE '%@%.%')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Açıklama ekle
ALTER TABLE users COMMENT = 'Sistem kullanıcıları ve kimlik doğrulama bilgileri';
```

**Alan Açıklamaları:**
```sql
ALTER TABLE users MODIFY COLUMN id BIGINT COMMENT 'Birincil anahtar - Otomatik artış';
ALTER TABLE users MODIFY COLUMN name VARCHAR(255) COMMENT 'Kullanıcı adı';
ALTER TABLE users MODIFY COLUMN email VARCHAR(255) COMMENT 'E-posta adresi (benzersiz)';
ALTER TABLE users MODIFY COLUMN password VARCHAR(255) COMMENT 'BCrypt ile şifrelenmiş şifre';
ALTER TABLE users MODIFY COLUMN role ENUM('USER', 'ADMIN', 'STUDENT') COMMENT 'Kullanıcı rolü';
ALTER TABLE users MODIFY COLUMN verified BOOLEAN COMMENT 'E-posta doğrulama durumu';
ALTER TABLE users MODIFY COLUMN reset_token VARCHAR(255) COMMENT 'Şifre sıfırlama tokeni';
ALTER TABLE users MODIFY COLUMN verification_token VARCHAR(255) COMMENT 'E-posta doğrulama tokeni';
```

---

### 2. Books Tablosu

```sql
CREATE TABLE IF NOT EXISTS books (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(255) NOT NULL,
  isbn VARCHAR(20) UNIQUE,
  category VARCHAR(100),
  page_count INT,
  image_url VARCHAR(1000),
  stock INT DEFAULT 5,
  created_by VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_title (title),
  INDEX idx_author (author),
  INDEX idx_category (category),
  INDEX idx_isbn (isbn),
  
  CONSTRAINT chk_stock CHECK (stock >= 0),
  CONSTRAINT chk_page_count CHECK (page_count > 0 OR page_count IS NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Açıklama ekle
ALTER TABLE books COMMENT = 'Kütüphane kitap kataloğu';
```

**Alan Açıklamaları:**
```sql
ALTER TABLE books MODIFY COLUMN id BIGINT COMMENT 'Birincil anahtar';
ALTER TABLE books MODIFY COLUMN title VARCHAR(255) COMMENT 'Kitap başlığı (zorunlu)';
ALTER TABLE books MODIFY COLUMN author VARCHAR(255) COMMENT 'Yazar adı (zorunlu)';
ALTER TABLE books MODIFY COLUMN isbn VARCHAR(20) COMMENT 'Uluslararası Standart Kitap Numarası (benzersiz)';
ALTER TABLE books MODIFY COLUMN category VARCHAR(100) COMMENT 'Kitap kategorisi (Edebiyat, Bilim vb.)';
ALTER TABLE books MODIFY COLUMN page_count INT COMMENT 'Sayfa sayısı';
ALTER TABLE books MODIFY COLUMN image_url VARCHAR(1000) COMMENT 'Kapak resmi URL''i';
ALTER TABLE books MODIFY COLUMN stock INT COMMENT 'Mevcut stok sayısı (minimum 0)';
ALTER TABLE books MODIFY COLUMN created_by VARCHAR(255) COMMENT 'Kitabı sistem''e ekleyen kişi (email)';
```

---

### 3. Borrows Tablosu

```sql
CREATE TABLE IF NOT EXISTS borrows (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  borrow_date DATE NOT NULL,
  due_date DATE NOT NULL,
  return_date DATE NULL,
  
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
  
  INDEX idx_user_id (user_id),
  INDEX idx_book_id (book_id),
  INDEX idx_return_date (return_date),
  
  CONSTRAINT chk_dates CHECK (borrow_date <= due_date),
  CONSTRAINT chk_return_date CHECK (return_date IS NULL OR return_date >= borrow_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Açıklama ekle
ALTER TABLE borrows COMMENT = 'Kitap ödünç verme işlemleri ve geçmişi';
```

**Alan Açıklamaları:**
```sql
ALTER TABLE borrows MODIFY COLUMN id BIGINT COMMENT 'Birincil anahtar';
ALTER TABLE borrows MODIFY COLUMN user_id BIGINT COMMENT 'Kitabı ödünç alan kullanıcı (FK)';
ALTER TABLE borrows MODIFY COLUMN book_id BIGINT COMMENT 'Ödünç alınan kitap (FK)';
ALTER TABLE borrows MODIFY COLUMN borrow_date DATE COMMENT 'Ödünç alma tarihi';
ALTER TABLE borrows MODIFY COLUMN due_date DATE COMMENT 'Son teslim tarihi';
ALTER TABLE borrows MODIFY COLUMN return_date DATE COMMENT 'Iade tarihi (NULL = henüz iade edilmedi)';
```

---

### 4. Penalties Tablosu (Ceza/Para Cezası)

```sql
CREATE TABLE IF NOT EXISTS penalties (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  borrow_id BIGINT,
  book_title VARCHAR(255),
  days_overdue INT NOT NULL,
  fine_amount DECIMAL(10, 2) NOT NULL,
  paid BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (borrow_id) REFERENCES borrows(id) ON DELETE SET NULL,
  
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at),
  INDEX idx_paid (paid),
  
  CONSTRAINT chk_days_overdue CHECK (days_overdue > 0),
  CONSTRAINT chk_fine_amount CHECK (fine_amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Açıklama ekle
ALTER TABLE penalties COMMENT = 'Geç iade cezaları ve ödeme geçmişi';
```

**Alan Açıklamaları:**
```sql
ALTER TABLE penalties MODIFY COLUMN id BIGINT COMMENT 'Birincil anahtar';
ALTER TABLE penalties MODIFY COLUMN user_id BIGINT COMMENT 'Cezalandırılan kullanıcı (FK)';
ALTER TABLE penalties MODIFY COLUMN borrow_id BIGINT COMMENT 'İlişkili ödünç işlemi (FK, nullable)';
ALTER TABLE penalties MODIFY COLUMN book_title VARCHAR(255) COMMENT 'Geç iade edilen kitabın adı';
ALTER TABLE penalties MODIFY COLUMN days_overdue INT COMMENT 'Gecikme gün sayısı';
ALTER TABLE penalties MODIFY COLUMN fine_amount DECIMAL(10, 2) COMMENT 'Ceza tutarı (TL)';
ALTER TABLE penalties MODIFY COLUMN paid BOOLEAN COMMENT 'Ceza ödeme durumu';
```

---

### 5. Audit Log Tablosu (İşlem İzleme)

```sql
CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  table_name VARCHAR(50),
  operation VARCHAR(10),
  record_id BIGINT,
  user_id BIGINT,
  changes LONGTEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_table_name (table_name),
  INDEX idx_operation (operation),
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Açıklama ekle
ALTER TABLE audit_log COMMENT = 'Sistem işlem günlüğü (TRIGGER ile otomatik doldurulur)';
```

---

## Tetikleyicilerin Kurulumu

### 1. Stok Azaltma Tetikleyicisi (Ödünç Alma)

**Amaç:** Yeni bir ödünç kaydı oluşturulduğunda kitap stoğunu otomatik olarak 1 azalt.

```sql
DELIMITER $$

CREATE TRIGGER tr_decrease_stock_on_borrow
AFTER INSERT ON borrows
FOR EACH ROW
BEGIN
  DECLARE msg VARCHAR(255);
  
  -- Trigger başladı loglaması
  INSERT INTO audit_log (table_name, operation, record_id, changes)
  VALUES ('borrows', 'INSERT_TRIGGER_STOCK', NEW.id, 
          CONCAT('Trigger başladı - Kitap ID: ', NEW.book_id, ', Stok azaltılacak'));
  
  -- Stoğu azalt (kilitsiz sorgu için recheck)
  UPDATE books 
  SET stock = stock - 1 
  WHERE id = NEW.book_id AND stock > 0;
  
  -- Güncelleme başarısız mı kontrol et
  IF ROW_COUNT() = 0 THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Kitap stokta yok veya bulunamadı!';
  END IF;
  
  -- Başarı logu
  INSERT INTO audit_log (table_name, operation, record_id, changes)
  VALUES ('books', 'UPDATE_TRIGGER', NEW.book_id, 
          'Stok 1 azaltıldı (ödünç alma)');
END$$

DELIMITER ;
```

**Test Etme:**
```sql
-- Tetikleyiciyi etkinleştirme durumunu kontrol et
SHOW TRIGGERS FROM library_db WHERE TRIGGER_NAME = 'tr_decrease_stock_on_borrow';

-- Manuel test
INSERT INTO borrows (user_id, book_id, borrow_date, due_date)
VALUES (1, 5, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY));

-- Stok kontrol et
SELECT id, title, stock FROM books WHERE id = 5;
-- Beklenen: stock 4 azaldı
```

---

### 2. Stok Artırma Tetikleyicisi (İade)

**Amaç:** Iade tarihi girildiğinde kitap stoğunu otomatik olarak 1 artır.

```sql
DELIMITER $$

CREATE TRIGGER tr_increase_stock_on_return
AFTER UPDATE ON borrows
FOR EACH ROW
BEGIN
  DECLARE new_stock INT;
  
  -- İade tarihi NULL'dan bir tarih değerine değişiyorsa
  IF OLD.return_date IS NULL AND NEW.return_date IS NOT NULL THEN
    
    -- Stok logu
    INSERT INTO audit_log (table_name, operation, record_id, changes)
    VALUES ('borrows', 'UPDATE_TRIGGER_RETURN', NEW.id,
            CONCAT('İade işlemi başladı - Kitap ID: ', NEW.book_id));
    
    -- Stoğu artır
    UPDATE books 
    SET stock = stock + 1 
    WHERE id = NEW.book_id;
    
    -- Güncellenen stok değerini al
    SELECT stock INTO new_stock FROM books WHERE id = NEW.book_id;
    
    -- Başarı logu
    INSERT INTO audit_log (table_name, operation, record_id, changes)
    VALUES ('books', 'UPDATE_TRIGGER', NEW.book_id, 
            CONCAT('Stok 1 artırıldı (iade). Yeni stok: ', new_stock));
    
  END IF;
END$$

DELIMITER ;
```

**Test Etme:**
```sql
-- Iade tarihi güncelle
UPDATE borrows 
SET return_date = CURDATE() 
WHERE id = 1 AND return_date IS NULL;

-- Stok kontrol et (artmış olmalı)
SELECT id, title, stock FROM books WHERE id = 5;
```

---

### 3. Otomatik Ceza Kaydı Tetikleyicisi

**Amaç:** İade tarihi girildiğinde, eğer geç iade ise otomatik ceza kaydı oluştur.

```sql
DELIMITER $$

CREATE TRIGGER tr_create_penalty_on_late_return
AFTER UPDATE ON borrows
FOR EACH ROW
BEGIN
  DECLARE days_late INT;
  DECLARE fine_amount DECIMAL(10, 2);
  DECLARE book_title_var VARCHAR(255);
  
  -- İade tarihi NULL'dan bir tarih değerine değişiyorsa
  IF OLD.return_date IS NULL AND NEW.return_date IS NOT NULL THEN
    
    -- Gecikme gün sayısını hesapla
    SET days_late = DATEDIFF(NEW.return_date, NEW.due_date);
    
    -- Eğer geç iade ise (days_late > 0)
    IF days_late > 0 THEN
      
      -- Kitap başlığını al
      SELECT title INTO book_title_var FROM books WHERE id = NEW.book_id;
      
      -- Ceza tutarını hesapla (günlük 10 TL)
      SET fine_amount = days_late * 10;
      
      -- Ceza kaydı oluştur
      INSERT INTO penalties (user_id, borrow_id, book_title, days_overdue, fine_amount, paid)
      VALUES (
        NEW.user_id,
        NEW.id,
        book_title_var,
        days_late,
        fine_amount,
        FALSE
      );
      
      -- Audit logu
      INSERT INTO audit_log (table_name, operation, record_id, changes)
      VALUES ('penalties', 'INSERT_TRIGGER', NEW.id,
              CONCAT('Geç iade cezası oluşturuldu: ', days_late, ' gün, ', 
                     fine_amount, ' TL'));
      
    ELSE
      -- Zamanında iade logu
      INSERT INTO audit_log (table_name, operation, record_id, changes)
      VALUES ('borrows', 'UPDATE_TRIGGER', NEW.id,
              'Zamanında iade - Ceza yok');
    END IF;
    
  END IF;
END$$

DELIMITER ;
```

**Test Etme:**
```sql
-- Geçmiş tarihte iade yapılmış ödünç kaydı oluştur
INSERT INTO borrows (user_id, book_id, borrow_date, due_date)
VALUES (1, 5, '2024-12-10', '2024-12-24');

-- Geç iade tarihi ile güncelle
UPDATE borrows 
SET return_date = '2024-12-30'  -- 6 gün geç
WHERE user_id = 1 AND book_id = 5 AND return_date IS NULL;

-- Penalty kaydını kontrol et
SELECT * FROM penalties 
WHERE user_id = 1 
ORDER BY created_at DESC 
LIMIT 1;

-- Beklenen sonuç:
-- days_overdue: 6
-- fine_amount: 60.00 (6 * 10)
```

---

### 4. Audit Log Tetikleyicisi (İşlem İzleme)

```sql
DELIMITER $$

CREATE TRIGGER tr_audit_book_insert
AFTER INSERT ON books
FOR EACH ROW
BEGIN
  INSERT INTO audit_log (table_name, operation, record_id, changes)
  VALUES (
    'books',
    'INSERT',
    NEW.id,
    CONCAT('Yeni kitap eklendi: ', NEW.title, ' - Yazar: ', NEW.author, 
           ' - Stok: ', NEW.stock)
  );
END$$

DELIMITER ;

-- Book Delete İçin
DELIMITER $$

CREATE TRIGGER tr_audit_book_delete
BEFORE DELETE ON books
FOR EACH ROW
BEGIN
  INSERT INTO audit_log (table_name, operation, record_id, changes)
  VALUES (
    'books',
    'DELETE',
    OLD.id,
    CONCAT('Kitap silindi: ', OLD.title, ' (ID: ', OLD.id, ')')
  );
END$$

DELIMITER ;

-- User Delete İçin
DELIMITER $$

CREATE TRIGGER tr_audit_user_delete
BEFORE DELETE ON users
FOR EACH ROW
BEGIN
  INSERT INTO audit_log (table_name, operation, record_id, changes)
  VALUES (
    'users',
    'DELETE',
    OLD.id,
    CONCAT('Kullanıcı silindi: ', OLD.email, ' (Rol: ', OLD.role, ')')
  );
END$$

DELIMITER ;
```

---

## Test Verisi Yükleme

### 1. Kullanıcı Verisi

```sql
-- Test kullanıcıları ekle
INSERT INTO users (name, email, password, role, verified) VALUES
('Admin Kullanıcı', 'admin@library.com', 
 '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KfzmAZNq1vSzHP4gfC', 'ADMIN', true),
('Ahmet Yılmaz', 'ahmet@example.com', 
 '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KfzmAZNq1vSzHP4gfC', 'USER', true),
('Fatma Demir', 'fatma@example.com', 
 '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KfzmAZNq1vSzHP4gfC', 'STUDENT', true),
('Mehmet Kaya', 'mehmet@example.com', 
 '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KfzmAZNq1vSzHP4gfC', 'USER', true);

-- Not: Password hash örneği (bcrypt format)
-- Gerçek şifre: 'password123'
-- Üretmek için: https://bcrypt-generator.com/
```

### 2. Kitap Verisi

```sql
INSERT INTO books (title, author, isbn, category, page_count, stock, created_by) VALUES
('Türk Edebiyatı Tarihi', 'Mehmet Kaplan', '978-9756493537', 'Edebiyat', 350, 5, 'admin@library.com'),
('Fizik Temelleri', 'Richard Feynman', '978-0465025275', 'Fen Bilimleri', 560, 3, 'admin@library.com'),
('Sapiens', 'Yuval Noah Harari', '978-0062316097', 'Tarihi', 520, 4, 'admin@library.com'),
('Algoritmalara Giriş', 'Thomas H. Cormen', '978-0262033848', 'Bilgisayar Bilimi', 1312, 2, 'admin@library.com'),
('İnsan Hakları', 'Stephen P. Marks', '978-0190498764', 'Hukuk', 400, 6, 'admin@library.com'),
('Psikoloji 101', 'Paul Bloom', '978-0393639049', 'Psikoloji', 480, 3, 'admin@library.com'),
('Ekonomi Temelleri', 'Paul Samuelson', '978-0070605380', 'Ekonomi', 750, 4, 'admin@library.com'),
('Quran Tarihi', 'Mustafa Öztürk', '978-9944999485', 'Din Bilimleri', 280, 5, 'admin@library.com');
```

### 3. Ödünç Verme Verisi

```sql
-- Geçmiş ödünç verme işlemleri
INSERT INTO borrows (user_id, book_id, borrow_date, due_date, return_date) VALUES
-- Zamanında iade edilmiş
(2, 1, '2024-12-01', '2024-12-15', '2024-12-14'),
(3, 2, '2024-12-05', '2024-12-19', '2024-12-18'),

-- Geç iade edilmiş (5 gün geç)
(2, 3, '2024-12-01', '2024-12-15', '2024-12-20'),

-- Hala ödünç alınmış (iade edilmemiş)
(3, 4, '2024-12-20', '2025-01-03', NULL),
(4, 5, '2024-12-22', '2025-01-05', NULL);

-- Tetikleyicileri test et
-- Kontrol: SELECT * FROM audit_log ORDER BY created_at DESC LIMIT 10;
```

---

## Veritabanı Bakımı

### 1. Tetikleyicileri Listeleme

```sql
-- Tüm tetikleyicileri göster
SHOW TRIGGERS FROM library_db;

-- Specifik tetikleyici hakkında bilgi al
SELECT 
  TRIGGER_NAME,
  TRIGGER_SCHEMA,
  EVENT_MANIPULATION,
  EVENT_OBJECT_TABLE,
  TRIGGER_TIME,
  CREATED
FROM INFORMATION_SCHEMA.TRIGGERS
WHERE TRIGGER_SCHEMA = 'library_db'
ORDER BY EVENT_OBJECT_TABLE, TRIGGER_NAME;
```

### 2. Tetikleyiciyi Silme

```sql
-- Belirli tetikleyiciyi sil
DROP TRIGGER IF EXISTS tr_decrease_stock_on_borrow;

-- Belirli tetikleyiciyi yeniden oluştur
-- (yukarıdaki script'i tekrar çalıştır)
```

### 3. Tetikleyiciyi Devre Dışı Bırakma

```sql
-- MySQL'de tetikleyiciyi tamamen devre dışı bırakamazsın,
-- ancak koşulu değiştirerek etkisiz hale getirebilirsin:

DELIMITER $$

CREATE TRIGGER tr_decrease_stock_on_borrow_DISABLED
AFTER INSERT ON borrows
FOR EACH ROW
BEGIN
  -- Boş trigger (hiçbir şey yapmaz)
END$$

DELIMITER ;

-- Sonra eski tetikleyiciyi sil ve yenisini akitifleştir
DROP TRIGGER tr_decrease_stock_on_borrow;
RENAME TABLE tr_decrease_stock_on_borrow_DISABLED TO tr_decrease_stock_on_borrow;
-- (Ancak bu RENAME işlemi TRIGGER için çalışmaz)
-- Daha iyi yol: Tetikleyiciyi sil ve yeniden oluştur
```

### 4. Tetikleyici Performans Kontrol

```sql
-- Tetikleyici işlem sayısını kontrol et
SELECT 
  TRIGGER_NAME,
  COUNT(*) AS total_operations
FROM INFORMATION_SCHEMA.TRIGGERS
WHERE TRIGGER_SCHEMA = 'library_db'
GROUP BY TRIGGER_NAME;

-- Audit log'da tetikleyici aktivitesini kontrol et
SELECT 
  operation,
  COUNT(*) AS count,
  MAX(created_at) AS last_triggered
FROM audit_log
WHERE operation LIKE '%TRIGGER%'
GROUP BY operation;
```

### 5. Veritabanı Tüm Yapı Denetimi

```sql
-- Tüm tablolara ait bilgi
SELECT 
  TABLE_NAME,
  COLUMN_COUNT,
  ROW_COUNT,
  DATA_LENGTH,
  INDEX_LENGTH
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'library_db'
GROUP BY TABLE_NAME;

-- Foreign key'leri kontrol et
SELECT 
  CONSTRAINT_NAME,
  TABLE_NAME,
  COLUMN_NAME,
  REFERENCED_TABLE_NAME,
  REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'library_db'
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Index'leri kontrol et
SELECT 
  TABLE_NAME,
  INDEX_NAME,
  COLUMN_NAME,
  SEQ_IN_INDEX,
  NON_UNIQUE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'library_db'
ORDER BY TABLE_NAME, INDEX_NAME;
```

### 6. Veritabanı Yedekleme

```bash
# MySQL ile veritabanı yedekle (Terminal/CMD)
mysqldump -u root -p library_db > library_db_backup.sql

# Yedekten geri yükle
mysql -u root -p library_db < library_db_backup.sql

# Belirli tablo(lar) için yedek
mysqldump -u root -p library_db books users > tables_backup.sql

# Yalnızca schema (tablo yapısı) yedekle
mysqldump -u root -p --no-data library_db > schema_only.sql

# Yalnızca data yedekle
mysqldump -u root -p --no-create-info library_db > data_only.sql
```

### 7. Veritabanı Iyileştirme

```sql
-- Tüm tablolar için optimize et
OPTIMIZE TABLE users, books, borrows, penalties, audit_log;

-- Fragmented tablolları onar
REPAIR TABLE users, books, borrows, penalties, audit_log;

-- Table statistics'i güncelle
ANALYZE TABLE users, books, borrows, penalties, audit_log;

-- Query performansını kontrol et
EXPLAIN SELECT * FROM books WHERE category = 'Edebiyat';

-- Slow query log'unu etkinleştir
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

### 8. Veritabanı İzleme ve Sorun Giderme

```sql
-- Açık bağlantıları göster
SHOW PROCESSLIST;

-- Uzun süreli işlemleri sorgula
SELECT * FROM INFORMATION_SCHEMA.PROCESSLIST 
WHERE TIME > 300 AND COMMAND != 'Sleep';

-- Belirli işlemi iptal et
KILL QUERY <process_id>;

-- Veritabanı boyutunu kontrol et
SELECT 
  TABLE_SCHEMA,
  ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS size_mb
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'library_db'
GROUP BY TABLE_SCHEMA;

-- Tablo satır sayısını kontrol et
SELECT 
  TABLE_NAME,
  TABLE_ROWS
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'library_db';
```

---

## Otomatik Veritabanı Kurulum Script'i

Tüm yukarıdaki komutları bir dosyaya yazarak otomatik kurulum yapabilirsiniz:

**Dosya: `full_database_setup.sql`**

```sql
-- ============================================================
-- Akıllı Kütüphane Yönetim Sistemi - Veritabanı Kurulum
-- ============================================================
-- Tarih: 2024-12-24
-- Sürüm: 1.0.0
-- ============================================================

-- 1. VERİTABANINI OLUŞTUR
CREATE DATABASE IF NOT EXISTS library_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE library_db;

-- 2. TABLOYU OLUŞTUR
-- [Tüm CREATE TABLE komutlarını buraya ekle]

-- 3. TETİKLEYİCİLERİ OLUŞTUR
-- [Tüm TRIGGER komutlarını buraya ekle]

-- 4. TEST VERİSİ EKLE
-- [Tüm INSERT komutlarını buraya ekle]

-- 5. DOĞRULAMA
SELECT 'Veritabanı kurulumu tamamlandı!' AS status;

SELECT COUNT(*) AS user_count FROM users;
SELECT COUNT(*) AS book_count FROM books;
SELECT COUNT(*) AS borrow_count FROM borrows;

SHOW TRIGGERS FROM library_db;
```

**Kurulumu Çalıştırma:**

```bash
# MySQL'de çalıştır
mysql -u root -p < full_database_setup.sql

# veya MySQL Client'ta
mysql> source full_database_setup.sql;
```

---

**Hazırlandı:** 24 Aralık 2024  
**Versiyon:** 1.0.0
