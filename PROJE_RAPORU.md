# Akıllı Kütüphane Yönetim Sistemi - Proje Raporu

## Dersin Bilgileri

**Üniversite:** Karadeniz Teknik Üniversitesi  
**Fakülte:** Of Teknoloji Fakültesi  
**Bölüm:** Yazılım Mühendisliği  
**Ders:** YZM2017 - Veri Tabanı ve Yönetimi  

**Hazırlayan:** Yusuf Tan Durmuş - 445841  
**Dersi Veren:** Hakan Aydın  
**Teslim Tarihi:** 24 Aralık 2024

---

## 1. Giriş ve Projenin Amacı

Kütüphanelerdeki manuel takip yöntemleri (defter, Excel vb.) hata ve zaman kaybına yol açmaktadır. Bu proje, kitap envanteri ve ödünç-iade süreçlerini dijitalleştirerek insan hatasını azaltmayı ve verimliliği artırmayı amaçlayan bir web tabanlı yönetim sistemidir.

### Temel Hedefler

- Envanter ve stok takibinin anlık yapılması
- Ödünç alma/iade işlemlerinin kayıt altına alınması
- Geç iade durumlarında otomatik ceza hesaplama
- Ders kapsamındaki SQL Trigger, Normalizasyon ve İlişkisel Tasarım konularını uygulamak

---

## 2. Kullanılan Teknolojiler ve Araçlar

Proje Katmanlı Mimari (Layered Architecture) ile geliştirilmiştir:

| Teknoloji | Sürüm | Amacı |
|-----------|-------|-------|
| Java | 21 | Backend programlama dili |
| Spring Boot | 3.5.0 | REST API ve web framework'ü |
| MySQL | 8.0+ | İlişkisel veritabanı |
| Hibernate/JPA | Latest | ORM (Object-Relational Mapping) |
| HTML/CSS/JavaScript | - | Frontend arayüzü |
| Maven | 3.6+ | Build ve bağımlılık yönetimi |
| JWT | 0.11.5 | Kimlik doğrulama ve authorization |
| BCrypt | Built-in | Şifre hashleme |

---

## 3. Sistem Analizi ve Tasarım

### 3.1. Veritabanı Tasarımı

Sistem 5 ana tablodan oluşur:

1. **users** - Kullanıcı kayıtları (Admin, Öğrenci, Personel)
2. **books** - Kitap envanteri
3. **borrows** - Ödünç işlemleri
4. **penalties** - Geç iade cezaları
5. **audit_log** - İşlem izleme günlüğü

### 3.2. ER Diyagramı

```
USERS (1) ←────→ (N) BORROWS ←────→ (1) BOOKS
  │                      │
  │                      ├─→ (1) PENALTIES (N)
  └──────────────────────┘
  
AUDIT_LOG (Tüm işlemleri kaydeder)
```

#### Tablo Yapıları

**USERS:**
- id (PK)
- name, email (UNIQUE)
- password (BCrypt hashed)
- role (ENUM: ADMIN, USER, STUDENT)
- verified, reset_token, verification_token
- created_at

**BOOKS:**
- id (PK)
- isbn (UNIQUE), title, author
- category, page_count
- stock (INT, >= 0)
- image_url, created_by, created_at

**BORROWS:**
- id (PK)
- user_id (FK), book_id (FK)
- borrow_date, due_date, return_date (NULL ise iade yapılmamış)
- created_at

**PENALTIES:**
- id (PK)
- user_id (FK), borrow_id (FK)
- days_overdue, fine_amount
- paid (BOOL), created_at

**AUDIT_LOG:**
- id (PK)
- table_name, operation (INSERT/UPDATE/DELETE)
- record_id, user_id, description, timestamp

### 3.3. Normalizasyon (3NF)

**1NF (Atomic Values):**
- Tüm alanlar atomik değerler içerir
- Tekrarlayan veri grupları yok

**2NF (Remove Partial Dependencies):**
- Tüm non-key alanlar birincil anahtara tam bağımlı

**3NF (Remove Transitive Dependencies):**
- Geçişli bağımlılıklar kaldırıldı
- Örnek: Borrows tablosunda yazar bilgisi tutulmaz; yazar sadece Books tablosunda yer alır

### 3.4. Katmanlı Mimari

```
┌─────────────────────────────────┐
│  CONTROLLER LAYER               │
│  (HTTP isteklerini karşılar)    │
├─────────────────────────────────┤
│  SERVICE LAYER                  │
│  (İş mantığı)                   │
├─────────────────────────────────┤
│  REPOSITORY LAYER               │
│  (Veri erişimi - JPA)           │
├─────────────────────────────────┤
│  DATABASE LAYER                 │
│  (MySQL)                        │
└─────────────────────────────────┘
```

**Katmanlar:**
1. **Controller:** HTTP isteklerini karşılar (AuthController, BookController, BorrowController vb.)
2. **Service:** İş mantığı, stok kontrolü, ceza hesaplama
3. **Repository:** JPA ile CRUD operasyonları
4. **Database:** MySQL ile veri saklama

---

## 4. Geliştirilen Özellikler ve Tetikleyiciler

### 4.1. Veritabanı Tetikleyicileri (Triggers)

Sistemin "akıllı" yönü tetikleyicilerle sağlanmıştır:

#### 1. Stok Azaltma (tr_decrease_stock_on_borrow)
- **Olay:** borrows tablosuna INSERT
- **İşlem:** İlgili kitabın stokunu 1 azalt
- **Kontrol:** Stok 0'dan düşerse hata döner
- **Günlük:** Audit_log'a kaydı yazılır

#### 2. Stok Artırma (tr_increase_stock_on_return)
- **Olay:** borrows tablosunda return_date güncellendiğinde
- **İşlem:** İlgili kitabın stokunu 1 artır
- **Günlük:** Audit_log'a kaydı yazılır

#### 3. Otomatik Ceza (tr_create_penalty_on_late_return)
- **Olay:** İade tarihi vade tarihinden sonra ise
- **Hesaplama:** `ceza = geç_gün_sayısı × 10 TL`
- **Örnek:** 3 gün geç = 30 TL ceza
- **Kayıt:** penalties tablosuna otomatik eklenir

#### 4. İşlem İzleme (Audit Logging)
- **Neler Kaydedilir:** Tüm INSERT, UPDATE, DELETE işlemleri
- **Bilgiler:** Tablo adı, işlem tipi, kayıt ID, kullanıcı, tarih/saat
- **Amaç:** Sistem denetimi ve uyumsuzluk tespiti

### 4.2. Ödünç Alma İş Akışı

```
1. Kullanıcı kitap ödünç alma isteği gönderir
   ↓
2. Stok kontrolü yapılır (Service katmanında)
   ↓
3. Eğer stok yoksa işlem engellenir
   ↓
4. Eğer stok varsa BORROWS tablosuna INSERT yapılır
   ↓
5. Tetikleyici çalışır → BOOKS tablosundaki stok 1 azalır
   ↓
6. Audit_log'a işlem kaydı yazılır
   ↓
7. Yanıt döndürülür
```

### 4.3. İade ve Ceza İş Akışı

```
1. Kullanıcı kitap iade isteği gönderir
   ↓
2. İade tarihi güncelenir
   ↓
3. Tetikleyici çalışır:
   - Due date < return date mi kontrol edilir
   - Eğer evet ise ceza hesaplanır ve penalties'e yazılır
   - Stok 1 artırılır
   - Audit_log'a kaydı yazılır
   ↓
4. Yanıt döndürülür
```

### 4.4. Güvenlik

- **Şifre Hashing:** BCrypt ile güvenli şifre depolama
- **JWT Token:** 24 saat geçerlilik süresi
- **Role-Based Access:** Admin/User ayrımı
- **CORS:** Gerekli origin'lerin konfigürasyonu
- **SQL Injection Koruması:** Parametreli sorgular

---

## 5. Test Senaryoları

Postman ile test edilen temel senaryolar:

| Test No | Senaryo | Sonuç |
|---------|---------|-------|
| TC-01 | Kullanıcı Kaydı | ✅ Başarılı - Kullanıcı eklenir |
| TC-02 | E-posta Doğrulama | ✅ Başarılı - Token kontrol edilir |
| TC-03 | Giriş Yapma | ✅ Başarılı - JWT token üretilir |
| TC-04 | Kitap Arama | ✅ Başarılı - Sonuçlar listelenir |
| TC-05 | Kitap Ödünç Alma | ✅ Başarılı - Stok 1 azalır |
| TC-06 | Zamanında İade | ✅ Başarılı - Stok 1 artar, ceza yok |
| TC-07 | Geç İade | ✅ Başarılı - Otomatik ceza oluşur |
| TC-08 | Stoksuz İşlem | ✅ Başarılı - İşlem engellenir |
| TC-09 | Yetki Kontrolü | ✅ Başarılı - Yetkisiz erişim engellenir |
| TC-10 | Admin Kitap Eklemesi | ✅ Başarılı - Kitap eklenir |

---

## 6. Sonuç ve Kazanımlar

Bu proje sayesinde aşağıdaki konularda deneyim kazanıldı:

### Teknik Kazanımlar
- İlişkisel veritabanı tasarımı ve 3NF uygulaması
- SQL Trigger'ları ile iş mantığının veritabanı katmanına taşınması
- Spring Boot ve REST API geliştirme
- JPA/Hibernate ORM kullanımı
- JWT tabanlı kimlik doğrulama ve yetkilendirme

### Pratik Kazanımlar
- Katmanlı mimari (Layered Architecture) tasarımı
- Git ve sürüm kontrolü
- Postman ile API testi
- Hata yönetimi ve exception handling
- Veritabanı optimization

### Mühendislik Kazanımları
- Sistem analizi ve tasarımı
- İş gereksinimlerini teknik çözümlere dönüştürme
- Güvenlik ve veri koruması
- Performans ve ölçeklenebilirlik düşünmek

---

## 7. Kaynakça

1. Spring Boot Official Documentation - https://spring.io/projects/spring-boot
2. MySQL 8.0 Reference Manual - https://dev.mysql.com/doc/
3. Hibernate ORM Guide - https://hibernate.org/orm/documentation/
4. JWT.io - https://jwt.io/
5. Ders Notları - Hakan Aydın, YZM2017 - Veri Tabanı ve Yönetimi

---

**Hazırlandı:** 24 Aralık 2024  
**Versiyon:** 1.0.0  
**Dil:** Türkçe

