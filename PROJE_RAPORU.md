# AKILLI KÜTÜPHANE YÖNETİM SİSTEMİ - PROJE RAPORU

**Hazırlayan:** Yusuf Tan Durmuş (445841)  
**Ders:** Veri Tabanı ve Yönetimi  
**Dersi Veren:** Hakan Aydın  
**Teslim Tarihi:** 24 Aralık 2024

---

## 1. Projenin Amacı

Bu projenin temel amacı, kütüphanelerdeki manuel takip yöntemlerinin (defter, Excel vb.) yarattığı hata ve zaman kaybını önlemek, kitap envanteri ve ödünç-iade süreçlerini dijitalleştirerek verimliliği artırmaktır.

### İşlevsel ve Teknik Hedefler

- **Envanter Takibi:** Kitap stoklarının ve envanterin anlık olarak takip edilmesi
- **Süreç Dijitalleşmesi:** Ödünç alma ve iade işlemlerinin kayıt altına alınması
- **Otomasyon:** Geç iade durumlarında sistem tarafından otomatik ceza hesaplaması yapılması
- **Akademik Uygulama:** Ders kapsamında öğrenilen SQL Trigger, Normalizasyon ve İlişkisel Tasarım konularının pratik uygulamaya dökülmesi

---

## 2. Veri Tabanı Tasarımı ve Veri Modeli

Sistemin veri tabanı, veri tutarlılığını sağlamak ve tekrarları önlemek amacıyla **3. Normal Form (3NF)** kurallarına uygun olarak tasarlanmıştır. Veri tabanı 5 ana tablodan oluşmaktadır.

### Varlık İlişki Yapısı (ER Özeti)

**Users (Kullanıcılar)**
- Admin, Öğrenci ve Personel gibi rol tabanlı kullanıcı kayıtlarını tutar
- Borrows, Penalties ve Audit_Log tabloları ile 1:N (Bire-Çok) ilişkisi vardır

**Books (Kitaplar)**
- ISBN, stok, yazar ve kategori bilgilerini içeren envanter tablosudur
- Borrows tablosu ile 1:N ilişkisi vardır

**Borrows (Ödünçler)**
- Kullanıcılar ve Kitaplar arasındaki ilişkiyi sağlayan, ödünç/iade tarihlerini tutan tablodur
- Users ve Books tabloları ile N:1 (Çoktan-Bire) ilişkisi vardır

**Penalties (Cezalar)**
- Gecikme cezalarını tutar
- Users ve Borrows tabloları ile ilişkilidir

**Audit_Log (İşlem Günlüğü)**
- Sistemdeki kritik değişikliklerin (INSERT, UPDATE, DELETE) otomatik olarak kaydedildiği izleme tablosudur

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
5. Yusuf Tan Durmuş, "Akıllı Kütüphane Yönetim Sistemi", GitHub Repository
6. Ders Notları - Hakan Aydın, YZM2017 - Veri Tabanı ve Yönetimi

---

**Hazırlandı:** 24 Aralık 2024  
**Versiyon:** 1.0.0  
**Dil:** Türkçe

