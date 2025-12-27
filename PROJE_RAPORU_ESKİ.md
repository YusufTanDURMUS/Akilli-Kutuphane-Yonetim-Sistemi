# AKILLI KÜTÜPHANE YÖNETİM SİSTEMİ PROJE RAPORU

**Hazırlayan:** Yusuf Tan Durmuş (445841)  
**Ders:** Veri Tabanı ve Yönetimi

---

## 1. Projenin Amacı

Bu projenin temel amacı, kütüphanelerdeki manuel takip yöntemlerinin (defter, Excel vb.) yarattığı hata ve zaman kaybını önlemek, kitap envanteri ve ödünç-iade süreçlerini dijitalleştirerek verimliliği artırmaktır.

### İşlevsel ve Teknik Hedefler:

- **Envanter Takibi:** Kitap stoklarının ve envanterin anlık olarak takip edilmesi.

- **Süreç Dijitalleşmesi:** Ödünç alma ve iade işlemlerinin kayıt altına alınması.

- **Otomasyon:** Geç iade durumlarında sistem tarafından otomatik ceza hesaplaması yapılması.

- **Akademik Uygulama:** Ders kapsamında öğrenilen SQL Trigger, Normalizasyon ve İlişkisel Tasarım konularının pratik uygulamaya dökülmesi.

---

## 2. Veri Tabanı Tasarımı ve Veri Modeli

Sistemin veri tabanı, veri tutarlılığını sağlamak ve tekrarları önlemek amacıyla **3. Normal Form (3NF)** kurallarına uygun olarak tasarlanmıştır. Veri tabanı 5 ana tablodan oluşmaktadır.

### Varlık İlişki Yapısı (ER Özeti):

**Users (Kullanıcılar):** Admin, Öğrenci ve Personel gibi rol tabanlı kullanıcı kayıtlarını tutar. Borrows, Penalties ve Audit_Log tabloları ile 1:N (Bire-Çok) ilişkisi vardır.

**Books (Kitaplar):** ISBN, stok, yazar ve kategori bilgilerini içeren envanter tablosudur.

**Borrows (Ödünçler):** Kullanıcılar ve Kitaplar arasındaki ilişkiyi sağlayan, ödünç/iade tarihlerini tutan tablodur.

**Penalties (Cezalar):** Gecikme cezalarını tutar. Users ve Borrows tabloları ile ilişkilidir.

**Audit_Log (İşlem Günlüğü):** Sistemdeki kritik değişikliklerin (INSERT, UPDATE, DELETE) otomatik olarak kaydedildiği izleme tablosudur.

### Varlık İlişki Diyagramı (ER Diagram)

```
                    ┌──────────────────────────────┐
                    │          USERS               │
                    ├──────────────────────────────┤
                    │ PK  id (BIGINT)              │
                    │     name (VARCHAR)           │
                    │ UK  email (VARCHAR)          │
                    │     password (VARCHAR)       │
                    │     role (ENUM)              │
                    │     verified (BOOLEAN)       │
                    │     reset_token (VARCHAR)    │
                    │     created_at (TIMESTAMP)   │
                    │     updated_at (TIMESTAMP)   │
                    └──────────┬────────────────────┘
                               │
                    ┌──────────┴──────────┬──────────────┐
                    │                     │              │
                    │ 1:N                 │ 1:N          │ 1:N
                    │                     │              │
        ┌───────────▼──────────┐ ┌────────▼─────────────┐ ┌─────────▼───────────┐
        │    BORROWS           │ │    PENALTIES        │ │   AUDIT_LOG         │
        ├──────────────────────┤ ├─────────────────────┤ ├─────────────────────┤
        │ PK  id (BIGINT)      │ │ PK  id (BIGINT)     │ │ PK  id (BIGINT)     │
        │ FK  user_id ◄────────┼─┤ FK  user_id ◄──────┼─┤     table_name (VC) │
        │ FK  book_id ◄────┐   │ │ FK  borrow_id       │ │     operation (VC)  │
        │     borrow_date  │   │ │     days_overdue    │ │     record_id       │
        │     due_date     │   │ │     fine_amount     │ │     user_id (FK)    │
        │     return_date  │   │ │     paid (BOOL)     │ │     description     │
        │     created_at   │   │ │     created_at      │ │     timestamp       │
        │     updated_at   │   │ │     updated_at      │ │                     │
        └─────────┬────────┘   │ └─────────────────────┘ └─────────────────────┘
                  │            │
                  │            └────────────────────┐
                  │                                 │
                  │ N:1                             │
                  │                                 │
        ┌─────────▼──────────────────┐             │
        │      BOOKS                 │             │
        ├────────────────────────────┤             │
        │ PK  id (BIGINT)            │             │
        │ UK  isbn (VARCHAR)         │             │
        │     title (VARCHAR)        │             │
        │     author (VARCHAR)       │             │
        │     category (VARCHAR)     │             │
        │     page_count (INT)       │             │
        │     stock (INT)            │◄────────────┘
        │     image_url (VARCHAR)    │
        │     created_at (TIMESTAMP) │
        │     updated_at (TIMESTAMP) │
        └────────────────────────────┘

PK = Primary Key | FK = Foreign Key | UK = Unique Key
1:N = Bire-Çok İlişki | N:1 = Çoktan-Bire İlişki
```

### Tablo Detayları ve İlişkiler

#### USERS Tablosu

**Amaç:** Sistem kullanıcılarının bilgilerini ve kimlik doğrulamalarını depolamak

**Alan Açıklamaları:**
- **id:** Benzersiz kullanıcı tanımlayıcısı
- **name:** Kullanıcının tam adı
- **email:** E-posta adresi (UNIQUE - benzersiz)
- **password:** BCrypt ile hashlenen şifre
- **role:** Kullanıcı rolü (ADMIN, USER, STUDENT)
- **verified:** E-posta doğrulama durumu
- **reset_token:** Şifre sıfırlama tokeni
- **created_at / updated_at:** Kayıt ve güncelleme tarihleri

**İlişkiler:**
- Users (1) ← → (N) Borrows
- Users (1) ← → (N) Penalties
- Users (1) ← → (N) Audit_Log

---

#### BOOKS Tablosu

**Amaç:** Kütüphane envanterindeki kitapların bilgilerini yönetmek

**Alan Açıklamaları:**
- **id:** Benzersiz kitap tanımlayıcısı
- **isbn:** Uluslararası Standart Kitap Numarası (UNIQUE)
- **title:** Kitap başlığı
- **author:** Yazar adı
- **category:** Kitap kategorisi
- **page_count:** Sayfa sayısı
- **stock:** Mevcut stok miktarı (0 ve üzeri değer alabilir)
- **image_url:** Kitap kapağı resmi URL'i

**İlişkiler:**
- Books (1) ← → (N) Borrows

---

#### BORROWS Tablosu

**Amaç:** Kitap ödünç alma ve iade işlemlerini kayıt altına almak

**Alan Açıklamaları:**
- **id:** Benzersiz ödünç işlemi tanımlayıcısı
- **user_id (FK):** Ödünçleyen kullanıcıya referans
- **book_id (FK):** Ödünç alınan kitaba referans
- **borrow_date:** Ödünç alma tarihi
- **due_date:** Kitabın iade edilmesi gereken tarih (genellikle borrow_date + 7 gün)
- **return_date:** Kitabın gerçek iade tarihi (NULL ise henüz iade edilmemiş)

**İlişkiler:**
- Borrows (N) → (1) Users
- Borrows (N) → (1) Books
- Borrows (1) ← → (N) Penalties

---

#### PENALTIES Tablosu

**Amaç:** Geç iade cezalarını kaydı almak ve yönetmek

**Alan Açıklamaları:**
- **id:** Benzersiz ceza tanımlayıcısı
- **user_id (FK):** Ceza alan kullanıcıya referans
- **borrow_id (FK):** Hangi ödünç işleminden ceza alındığına referans
- **days_overdue:** Kaç gün geç iade edildiği
- **fine_amount:** Ceza tutarı (Gün × 10 TL)
- **paid:** Cezanın ödenip ödenmediği durumu

**Örnek Hesaplama:**
```
- Due Date: 24.12.2024
- Return Date: 28.12.2024
- Days Overdue: 4
- Fine Amount: 4 × 10 = 40 TL
```

**İlişkiler:**
- Penalties (N) → (1) Users
- Penalties (N) → (1) Borrows

---

#### AUDIT_LOG Tablosu

**Amaç:** Sistem genelindeki tüm kritik değişiklikleri kaydederek denetim ve izleme sağlamak

**Alan Açıklamaları:**
- **table_name:** Hangi tabloda işlem yapıldığı
- **operation:** İşlem türü (INSERT, UPDATE, DELETE)
- **record_id:** Etkilenen kayıt ID'si
- **user_id (FK):** İşlemi yapan kullanıcı
- **description:** İşlem açıklaması
- **timestamp:** İşlem gerçekleşme tarihi ve saati

**Örnek Kayıtlar:**
```
1. Table: borrows, Operation: INSERT, Record ID: 1, 
   Description: "Kitap ID:5 ödünç alındı", 
   Timestamp: 2024-12-24 14:30:45

2. Table: books, Operation: UPDATE, Record ID: 5,
   Description: "Stok: 5 → 4 (ödünç alma)",
   Timestamp: 2024-12-24 14:30:45

3. Table: penalties, Operation: INSERT, Record ID: 1,
   Description: "Geç iade cezası: 40 TL",
   Timestamp: 2024-12-28 16:45:20
```

**İlişkiler:**
- Audit_Log (N) → (1) Users

---

### İlişkisel Bütünlük Kuralları

| Referans | Bağlantı | Açıklama |
|----------|----------|----------|
| Borrows.user_id → Users.id | ON DELETE RESTRICT | Kullanıcı silinirse ödünç kayıtları da silinir |
| Borrows.book_id → Books.id | ON DELETE RESTRICT | Kitap silinirse ödünç kayıtları da silinir |
| Penalties.user_id → Users.id | ON DELETE CASCADE | Kullanıcı silinirse ceza kayıtları da silinir |
| Penalties.borrow_id → Borrows.id | ON DELETE CASCADE | Ödünç kaydı silinirse ceza da silinir |
| Audit_Log.user_id → Users.id | ON DELETE SET NULL | Kullanıcı silinirse işlem kaydına NULL yazılır |

---

## 3. Veri Tabanı Nesneleri (Trigger - Tetikleyiciler)

Projenin "Akıllı" yönünü desteklemek ve iş mantığını veri tabanı katmanında optimize etmek için MySQL tetikleyicileri (Triggers) kullanılmıştır.

### Kullanılan Temel Tetikleyiciler

#### 1. Stok Düşürme (tr_decrease_stock_on_borrow)

- **İşlev:** Ödünç alma işlemi (INSERT into borrows) gerçekleştiğinde çalışır
- **Sonuç:** İlgili kitabın stok adedini otomatik olarak 1 azaltır ve Audit_log'a kayıt atar
- **Kontrol:** Stok 0'dan düşerse işlem engellenir

#### 2. Stok Artırma (tr_increase_stock_on_return)

- **İşlev:** İade işlemi yapıldığında (return_date güncellendiğinde) çalışır
- **Sonuç:** İlgili kitabın stok adedini 1 artırır
- **Günlük:** Audit_log'a işlem kaydı yazılır

#### 3. Otomatik Ceza Hesaplama (tr_create_penalty_on_late_return)

- **İşlev:** Kitap iade edilirken son teslim tarihi (due_date) ile iade tarihi karşılaştırılır
- **Koşul:** Eğer return_date > due_date ise tetiklenir
- **Hesaplama:** Geciken Gün × 10 TL formülü ile ceza hesaplanır
- **Kayıt:** Penalties tablosuna otomatik kayıt eklenir

#### 4. İşlem İzleme (Audit Logging)

- **Neler Kaydedilir:** Tüm INSERT, UPDATE, DELETE işlemleri
- **Bilgiler:** Tablo adı, işlem tipi, kayıt ID, kullanıcı, tarih/saat
- **Amaç:** Sistem denetimi ve uyumsuzluk tespiti

---

## 4. Teknik Mimari ve Katmanlı Yapı

Proje, kodun sürdürülebilirliğini artırmak için **Katmanlı Mimari (Layered Architecture)** prensibiyle Java Spring Boot üzerinde geliştirilmiştir.

### Mimari Katmanları

```
┌─────────────────────────────┐
│  CONTROLLER KATMANI         │
│  (REST API Uç Noktaları)    │
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│  SERVICE KATMANI            │
│  (İş Mantığı)               │
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│  REPOSITORY KATMANI         │
│  (Veri Erişimi - JPA)       │
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│  VERİ TABANI KATMANI        │
│  (MySQL)                    │
└─────────────────────────────┘
```

### Katmanların Görevleri

- **Controller Katmanı:** Dış dünyadan gelen HTTP isteklerini (REST API) karşılar (Ör: AuthController, BookController)
- **Service Katmanı:** İş mantığının yürütüldüğü katmandır (Stok kontrolü, ceza hesaplama mantığı vb.)
- **Repository Katmanı:** Hibernate/JPA kullanılarak veri tabanı ile iletişimin sağlandığı veri erişim katmanıdır

### Kullanılan Teknolojiler

| Teknoloji | Versiyon | Amaç |
|-----------|----------|------|
| Java | 21 | Backend programlama dili |
| Spring Boot | 3.5.0 | Web framework ve REST API |
| MySQL | 8.0+ | İlişkisel veri tabanı |
| Hibernate/JPA | Latest | ORM (Object-Relational Mapping) |
| JWT | 0.11.5 | Kimlik doğrulama |
| BCrypt | Built-in | Şifre hashleme |

---

## 5. Güvenlik ve Kimlik Doğrulama

Sistem güvenliği modern standartlara uygun olarak sağlanmıştır:

### Kimlik Doğrulama (Authentication)
- JWT (JSON Web Token) kullanılarak **24 saat geçerli token** üretilir
- API isteklerinde token doğrulama yapılır
- Hatalı token'lar reddedilir

### Parola Güvenliği
- Kullanıcı şifreleri veri tabanında açık metin olarak tutulmaz
- **BCrypt algoritması** ile hash'lenerek saklanır
- Her şifre farklı salt ile hash'lenir (rainbow table atağından koruma)

### Yetkilendirme (Authorization)
- Admin ve Normal Kullanıcı rolleri ayrıştırılır
- Rol tabanlı erişim kontrolü (RBAC) sağlanır
- Yetkisiz kullanıcılar 403 Forbidden hatası alır

### Ek Güvenlik Önlemleri
- SQL Injection koruması (parametreli sorgular)
- CORS (Cross-Origin Resource Sharing) yapılandırması
- E-posta doğrulama mekanizması

---

## 6. Test Senaryoları ve Sonuçları

Geliştirilen sistemin fonksiyonları Postman ve arayüz üzerinden test edilmiş, aşağıdaki senaryolar başarıyla sonuçlanmıştır:

| Test No | Senaryo | Beklenen Sonuç | Durum |
|---------|---------|----------------|-------|
| TC-01 | Kullanıcı Kaydı | Yeni kullanıcı eklenir, şifre hash'lenir | ✅ Başarılı |
| TC-02 | Kitap Arama | Girilen isme göre doğru sonuçlar listelenir | ✅ Başarılı |
| TC-03 | Ödünç Alma | Borrows kaydı oluşur, stok otomatik 1 azalır (Trigger) | ✅ Başarılı |
| TC-04 | Stoksuz İşlem | Stok 0 ise işlem engellenir, hata döner | ✅ Başarılı |
| TC-05 | Geç İade ve Ceza | İade tarihi geçmişse otomatik ceza tablosu oluşur | ✅ Başarılı |
| TC-06 | Yetkisiz Erişim | Normal kullanıcı Admin paneline erişemez (403 Hata) | ✅ Başarılı |

---

## 7. Sonuç ve Kazanımlar

Proje sonucunda ilişkisel veri tabanı tasarımı, 3NF normalizasyon kuralları ve Trigger kullanımı ile iş mantığının veri tabanı seviyesine taşınması konularında deneyim kazanılmıştır.

### Teknik Kazanımlar

- İlişkisel veri tabanı tasarımı ve 3NF uygulaması
- MySQL Trigger'ları ile iş mantığının optimizasyonu
- Spring Boot ve JPA/Hibernate entegrasyonu
- JWT tabanlı güvenli kimlik doğrulama
- RESTful API tasarımı ve geliştirmesi

### Pratik Kazanımlar

- Katmanlı mimari (Layered Architecture) uygulaması
- Veri tabanı normalizasyon pratiği
- Git ve sürüm kontrolü kullanımı
- Postman ile API testi
- Hata yönetimi ve exception handling

### Mühendislik Kazanımları

- Sistem analizi ve tasarımı
- İş gereksinimlerini teknik çözümlere dönüştürme
- Veri tutarlılığı ve bütünlüğü sağlama
- Güvenlik ve veri koruma prensipleri
- Kod kalitesi ve sürdürülebilirlik

---

## Kaynakça

1. Spring Boot Official Documentation - https://spring.io/projects/spring-boot
2. MySQL 8.0 Reference Manual - https://dev.mysql.com/doc/
3. Hibernate ORM User Guide - https://hibernate.org/orm/documentation/
4. JWT.io - JSON Web Tokens - https://jwt.io/


   Yusuf Tan Durmuş, "Akıllı Kütüphane Yönetim Sistemi", GitHub Repository
   Ders Notları - Hakan Aydın, YZM2017 - Veri Tabanı ve Yönetimi

---

**Hazırlandı:** 24 Aralık 2025


