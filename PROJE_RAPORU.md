# AKILLI KÜTÜPHANE YÖNETİM SİSTEMİ PROJE RAPORU

**Ders:** YZM2017 - Veri Tabanı ve Yönetimi  
**Hazırlayan:** Yusuf Tan Durmuş (445841)  
**Dersi Veren:** Hakan Aydın  
**Teslim Tarihi:** 24 Aralık 2024

---

## 1. GİRİŞ VE PROJENİN AMACI

Bu projenin temel amacı, kütüphanelerdeki manuel takip yöntemlerinin (defter, Excel vb.) yarattığı hata ve zaman kaybını önlemek; kitap envanteri ve ödünç-iade süreçlerini dijitalleştirerek verimliliği artırmaktır. Modern yazılım geliştirme prensiplerine uygun, çalışan bir "Akıllı Kütüphane Yönetim Sistemi" geliştirilmiştir.

### Projenin Teknik ve İşlevsel Hedefleri:

- **Envanter Takibi:** Kitap stoklarının ve envanterin anlık olarak takip edilmesi.

- **Süreç Dijitalleşmesi:** Ödünç alma ve iade işlemlerinin kayıt altına alınması.

- **Otomasyon:** Geç iade durumlarında sistem tarafından otomatik ceza hesaplaması yapılması.

- **Akademik Uygulama:** Ders kapsamında öğrenilen SQL Trigger, Normalizasyon ve İlişkisel Tasarım konularının pratik uygulamaya dökülmesi.

---

## 2. SİSTEM ANALİZİ VE TASARIM

### 2.1. Veri Tabanı Tasarımı ve Veri Modeli

Sistemin veri tabanı, veri tutarlılığını sağlamak ve tekrarları önlemek amacıyla **3. Normal Form (3NF)** kurallarına uygun olarak tasarlanmıştır. Veri tabanı 5 ana tablodan oluşmaktadır.

### 2.2. Varlık İlişki Yapısı (ER Özeti)

**Users (Kullanıcılar):** Admin, Öğrenci ve Personel gibi rol tabanlı kullanıcı kayıtlarını tutar.

**Books (Kitaplar):** ISBN, stok, yazar ve kategori bilgilerini içeren envanter tablosudur.

**Borrows (Ödünçler):** Kullanıcılar ve Kitaplar arasındaki ilişkiyi sağlayan köprü tablodur.

**Penalties (Cezalar):** Gecikme cezalarını tutar.

**Audit_Log:** Sistemdeki kritik değişikliklerin izlendiği tablodur.

---

### 2.3. ER Tabloları (Detaylı)

| Alan | Tip | Anahtar | Not |
|------|-----|---------|-----|
| id | BIGINT | PK | Birincil anahtar - Otomatik artış |
| name | VARCHAR(255) | - | Kullanıcı adı (zorunlu) |
| email | VARCHAR(255) | UK | E-posta adresi (benzersiz) |
| password | VARCHAR(255) | - | BCrypt ile şifrelenmiş şifre |
| role | ENUM('USER', 'ADMIN', 'STUDENT') | - | Kullanıcı rolü |
| verified | BOOLEAN | - | E-posta doğrulama durumu |
| reset_token | VARCHAR(255) | - | Şifre sıfırlama tokeni |
| verification_token | VARCHAR(255) | - | E-posta doğrulama tokeni |
| created_at | TIMESTAMP | - | Oluşturulma tarihi |
| updated_at | TIMESTAMP | - | Güncellenme tarihi |

**İlişkiler:** Borrows (1:N), Penalties (1:N), Audit_Log (1:N)

#### BOOKS Tablosu

| Alan | Tip | Anahtar | Not |
|------|-----|---------|-----|
| id | BIGINT | PK | Birincil anahtar |
| title | VARCHAR(255) | - | Kitap başlığı (zorunlu) |
| author | VARCHAR(255) | - | Yazar adı (zorunlu) |
| isbn | VARCHAR(20) | UK | Uluslararası Standart Kitap Numarası |
| category | VARCHAR(100) | - | Kitap kategorisi |
| page_count | INT | - | Sayfa sayısı |
| stock | INT | - | Mevcut stok sayısı (minimum 0) |
| image_url | VARCHAR(1000) | - | Kapak resmi URL'i |
| created_by | VARCHAR(255) | - | Kitabı sistem'e ekleyen kişi (email) |
| created_at | TIMESTAMP | - | Oluşturulma tarihi |
| updated_at | TIMESTAMP | - | Güncellenme tarihi |

**İlişkiler:** Borrows (1:N)

#### BORROWS Tablosu

| Alan | Tip | Anahtar | Not |
|------|-----|---------|-----|
| id | BIGINT | PK | Birincil anahtar |
| user_id | BIGINT | FK | Kitabı ödünç alan kullanıcı |
| book_id | BIGINT | FK | Ödünç alınan kitap |
| borrow_date | DATE | - | Ödünç alma tarihi |
| due_date | DATE | - | Son teslim tarihi |
| return_date | DATE | - | İade tarihi (NULL = henüz iade edilmedi) |
| created_at | TIMESTAMP | - | Oluşturulma tarihi |
| updated_at | TIMESTAMP | - | Güncellenme tarihi |

**İlişkiler:** Users (N:1), Books (N:1), Penalties (1:N)  
**Foreign Keys:**
- `user_id` → Users(id) ON DELETE CASCADE
- `book_id` → Books(id) ON DELETE CASCADE

#### PENALTIES Tablosu

| Alan | Tip | Anahtar | Not |
|------|-----|---------|-----|
| id | BIGINT | PK | Birincil anahtar |
| user_id | BIGINT | FK | Cezalandırılan kullanıcı |
| borrow_id | BIGINT | FK | İlişkili ödünç işlemi |
| book_title | VARCHAR(255) | - | Geç iade edilen kitabın adı |
| days_overdue | INT | - | Gecikme gün sayısı |
| fine_amount | DECIMAL(10, 2) | - | Ceza tutarı (TL) |
| paid | BOOLEAN | - | Ceza ödeme durumu |
| created_at | TIMESTAMP | - | Oluşturulma tarihi |

**İlişkiler:** Users (N:1), Borrows (N:1)  
**Foreign Keys:**
- `user_id` → Users(id) ON DELETE CASCADE
- `borrow_id` → Borrows(id) ON DELETE SET NULL

#### AUDIT_LOG Tablosu

| Alan | Tip | Anahtar | Not |
|------|-----|---------|-----|
| id | BIGINT | PK | Birincil anahtar |
| table_name | VARCHAR(50) | - | Etkilenen tablo adı |
| operation | VARCHAR(10) | - | INSERT, UPDATE, DELETE |
| record_id | BIGINT | - | Etkilenen satırın ID'si |
| user_id | BIGINT | FK | İşlem yapan kullanıcı |
| changes | LONGTEXT | - | Yapılan değişikliklerin detayı |
| created_at | TIMESTAMP | - | İşlem tarihi |

**İlişkiler:** Users (N:1)  
**Foreign Keys:**
- `user_id` → Users(id) ON DELETE SET NULL

---

### 2.4. İlişkisel Bütünlük Özeti

| Ana Tablo | İlişkili Tablo | Tür | Silme Kuralı | Açıklama |
|-----------|----------------|-----|--------------|----------|
| Users | Borrows | 1:N | CASCADE | Kullanıcı silinince ödünçleri de silinir |
| Users | Penalties | 1:N | CASCADE | Kullanıcı silinince cezaları da silinir |
| Users | Audit_Log | 1:N | SET NULL | Kullanıcı silinince log kaydı kalır |
| Books | Borrows | 1:N | CASCADE | Kitap silinince ödünçleri de silinir |
| Borrows | Penalties | 1:N | SET NULL | Ödünç silinince ceza NULL olur |

---

## 3. GELİŞTİRİLEN ÖZELLİKLER VE MANTIKSAL YAPI

Projenin "Akıllı" yönünü desteklemek için MySQL tetikleyicileri (Triggers) kullanılmıştır. İş mantığı veri tabanı seviyesinde optimize edilmiştir.

### 3.1. Tetikleyiciler (Triggers)

#### 1. Stok Düşürme (tr_decrease_stock_on_borrow)

Ödünç alma işlemi (INSERT into borrows) gerçekleştiğinde çalışır. İlgili kitabın stok adedini otomatik olarak 1 azaltır.

```sql
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
END;
```

#### 2. Stok Artırma (tr_increase_stock_on_return)

İade işlemi yapıldığında (return_date güncellendiğinde) çalışır. İlgili kitabın stok adedini 1 artırır.

```sql
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
END;
```

#### 3. Otomatik Ceza Hesaplama (tr_create_penalty_on_late_return)

Kitap iade edilirken son teslim tarihi ile iade tarihi karşılaştırılır (return_date > due_date). Geciken gün sayısı üzerinden ceza hesaplanarak Penalties tablosuna otomatik kayıt atılır.

```sql
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
END;
```

---

## 4. TEKNİK MİMARİ VE KATMANLI YAPI

Proje, kodun sürdürülebilirliğini artırmak için Katmanlı Mimari (Layered Architecture) prensibiyle Java Spring Boot üzerinde geliştirilmiştir.

- **Controller Katmanı:** Dış dünyadan gelen HTTP isteklerini (REST API) karşılar.
- **Service Katmanı:** İş mantığının (Ceza hesaplama, stok kontrolü) yürütüldüğü katmandır.
- **Repository Katmanı:** Hibernate/JPA kullanılarak veri tabanı ile iletişimin sağlandığı katmandır.

### Kullanılan Teknolojiler:

- **Backend:** Java 21, Spring Boot 3.5.0
- **Veri Tabanı:** MySQL 8.0+
- **Güvenlik:** JWT (JSON Web Token 0.11.5) & BCrypt
- **ORM:** Hibernate/JPA
- **Build Tool:** Maven 3.6+

---

## 5. GÜVENLİK VE KİMLİK DOĞRULAMA

Sistem güvenliği için JWT tercih edilmiştir:

- **Kimlik Doğrulama:** Kullanıcı giriş yaptığında sunucu tarafından şifrelenmiş, 24 saat geçerli bir token üretilir.
- **Yetkilendirme:** Admin ve Normal Kullanıcı rolleri ayrıştırılmıştır. Admin paneline sadece ADMIN rolü erişebilir.
- **Parola Güvenliği:** Şifreler veri tabanında açık metin olarak değil, BCrypt ile hash'lenerek saklanır.
- **Token Yönetimi:** Her istek headers'da JWT token gönderilir ve sunucu tarafından doğrulanır.

---

## 6. TEST SENARYOLARI VE SONUÇLARI

Geliştirilen sistemin fonksiyonları Postman üzerinden test edilmiş ve aşağıdaki sonuçlar alınmıştır.

| Test No | Senaryo | Beklenen Sonuç | Durum |
|---------|---------|----------------|-------|
| TC-01 | Kullanıcı Kaydı | Yeni kullanıcı eklenir, şifre hash'lenir. | ✔ Başarılı |
| TC-02 | Kitap Arama | Girilen ada göre sonuçlar listelenir. | ✔ Başarılı |
| TC-03 | Ödünç Alma | Borrows kaydı oluşur, stok 1 azalır. | ✔ Başarılı |
| TC-04 | Stoksuz İşlem | Stok 0 ise işlem engellenir, hata döner. | ✔ Başarılı |
| TC-05 | Geç İade ve Ceza | Otomatik ceza oluşturulur. | ✔ Başarılı |
| TC-06 | Yetkisiz Erişim | Normal kullanıcı admin panele erişemez (403). | ✔ Başarılı |

---

## 7. SONUÇ VE KAZANIMLAR

Bu proje ile ilişkisel veri tabanı tasarımı, 3NF normalizasyon kuralları ve Trigger kullanımı konularında pratik deneyim kazanılmıştır. İş mantığının veri tabanı katmanına taşınmasının (Trigger kullanımı) veri bütünlüğünü nasıl koruduğu görülmüştür. Ayrıca Spring Boot ve MySQL entegrasyonu ile güvenli bir REST API altyapısı başarıyla kurulmuştur.

### Gelecek Çalışmalar:

- Sisteme online ödeme sistemi entegrasyonu
- Mobil arayüz eklenmesi

---

## Kaynakça

1. Spring Boot Official Documentation - https://spring.io/projects/spring-boot
2. MySQL 8.0 Reference Manual - https://dev.mysql.com/doc/
3. Hibernate ORM User Guide - https://hibernate.org/orm/documentation/
4. JWT.io - JSON Web Tokens - https://jwt.io/
5. Yusuf Tan Durmuş, "Akıllı Kütüphane Yönetim Sistemi", GitHub Repository
6. Ders Notları - Hakan Aydın, YZM2017 - Veri Tabanı ve Yönetimi

---

**Hazırlandı:** 27 Aralık 2025
