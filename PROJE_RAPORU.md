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

---

## 3. Veri Tabanı Nesneleri (Trigger - Tetikleyiciler)

Projenin "Akıllı" yönünü desteklemek ve iş mantığını veri tabanı katmanında optimize etmek için MySQL tetikleyicileri (Triggers) kullanılmıştır.

### Kullanılan Temel Tetikleyiciler:

#### Stok Düşürme (tr_decrease_stock_on_borrow):
- **İşlev:** Ödünç alma işlemi (INSERT into borrows) gerçekleştiğinde çalışır.
- **Sonuç:** İlgili kitabın stok adedini otomatik olarak 1 azaltır ve Audit_log'a kayıt atar.

#### Stok Artırma (tr_increase_stock_on_return):
- **İşlev:** İade işlemi yapıldığında (return_date güncellendiğinde) çalışır.
- **Sonuç:** İlgili kitabın stok adedini 1 artırır.

#### Otomatik Ceza Hesaplama (tr_create_penalty_on_late_return):
- **İşlev:** Kitap iade edilirken son teslim tarihi (due_date) ile iade tarihi karşılaştırılır. Eğer return_date > due_date ise tetiklenir.
- **Hesaplama:** Geciken Gün x 1.50 TL formülü ile ceza hesaplanır ve Penalties tablosuna otomatik kayıt eklenir.

---

## 4. Teknik Mimari ve Katmanlı Yapı

Proje, kodun sürdürülebilirliğini artırmak için **Katmanlı Mimari (Layered Architecture)** prensibiyle Java Spring Boot üzerinde geliştirilmiştir.

### Mimari Katmanları:

**Controller Katmanı:** Dış dünyadan gelen HTTP isteklerini (REST API) karşılar (Ör: AuthController, BookController).

**Service Katmanı:** İş mantığının yürütüldüğü katmandır (Stok kontrolü, ceza hesaplama mantığı vb.).

**Repository Katmanı:** Hibernate/JPA kullanılarak veri tabanı ile iletişimin sağlandığı veri erişim katmanıdır.

**Teknolojiler:** Backend için Java 21 & Spring Boot 3.5.0, Veri tabanı için MySQL 8.0+ kullanılmıştır.

---

## 5. Güvenlik ve Kimlik Doğrulama

Sistem güvenliği modern standartlara uygun olarak sağlanmıştır:

### Kimlik Doğrulama (Authentication):
JWT (JSON Web Token) kullanılarak 24 saat geçerli token üretilir ve API isteklerinde doğrulama yapılır.

### Parola Güvenliği:
Kullanıcı şifreleri veri tabanında açık metin olarak tutulmaz, BCrypt algoritması ile hash'lenerek saklanır.

### Yetkilendirme (Authorization):
Admin ve Normal Kullanıcı rolleri ayrıştırılarak uç noktalara erişim kontrolü sağlanmıştır.

### E-posta Bildirimleri ve Şifre Sıfırlama:
Sistem, kullanıcılara önemli olaylar hakkında otomatik e-posta bildirimleri gönderir:

- **Hoş Geldin E-postası:** Yeni kullanıcı kaydında sistem tarafından otomatik olarak karşılama e-postası gönderilir.

![Hoş Geldin E-postası](screenshots/email-welcome.png)

- **E-posta Doğrulama:** Kullanıcılar hesaplarını aktifleştirmek için e-posta doğrulaması yapmalıdır.

![E-posta Doğrulama Bildirimi](screenshots/email-verification.png)

- **Şifre Sıfırlama:** Kullanıcılar şifrelerini unuttukları zaman güvenli bir şekilde sıfırlayabilirler. Sistem, 24 saat geçerli bir sıfırlama linki içeren e-posta gönderir.

![Şifre Sıfırlama E-postası](screenshots/email-password-reset.png)

![Şifre Sıfırlama Ekranı](screenshots/password-reset-page.png)

---

## 6. Test Senaryoları ve Sonuçlar

Geliştirilen sistemin fonksiyonları Postman ve arayüz üzerinden test edilmiş, aşağıdaki senaryolar başarıyla sonuçlanmıştır.

| Test No | Senaryo | Beklenen Sonuç | Durum |
|---------|---------|----------------|-------|
| TC-01 | Kullanıcı Kaydı | Yeni kullanıcı eklenir, şifre hash'lenir. | ✔ Başarılı |
| TC-02 | Kitap Arama | Girilen isme göre doğru sonuçlar listelenir. | ✔ Başarılı |
| TC-03 | Ödünç Alma | Borrows kaydı oluşur, stok otomatik 1 azalır (Trigger). | ✔ Başarılı |
| TC-04 | Stoksuz İşlem | Stok 0 ise işlem engellenir, hata döner. | ✔ Başarılı |
| TC-05 | Geç İade ve Ceza | İade tarihi geçmişse otomatik ceza tablosu oluşur. | ✔ Başarılı |
| TC-06 | Yetkisiz Erişim | Normal kullanıcı Admin paneline erişemez (403 Hata). | ✔ Başarılı |

### Test Ekran Görüntüleri:

**Yetkisiz Erişim Testi (TC-06):**  
Normal bir kullanıcı, admin yetkisi gerektiren endpoint'e erişmeye çalıştığında sistem 403 Forbidden hatası döndürür.

![Postman API Test - 403 Forbidden](screenshots/postman-403-forbidden.png)

---

## 7. Sonuç ve Kazanımlar

Proje sonucunda ilişkisel veri tabanı tasarımı, 3NF normalizasyon kuralları ve Trigger kullanımı ile iş mantığının veri tabanı seviyesine taşınması konularında deneyim kazanılmıştır. Ayrıca Spring Boot ve MySQL entegrasyonu ile güvenli bir REST API altyapısı kurulmuştur.

### Teknik Kazanımlar:
- İlişkisel veri tabanı tasarımı ve 3NF uygulaması
- MySQL Trigger'ları ile iş mantığının optimizasyonu
- Spring Boot ve JPA/Hibernate entegrasyonu
- JWT tabanlı güvenli kimlik doğrulama
- RESTful API tasarımı ve geliştirmesi

### Pratik Kazanımlar:
- Katmanlı mimari (Layered Architecture) uygulaması
- Veri tabanı normalizasyon pratiği
- Git ve sürüm kontrolü kullanımı
- Postman ile API testi
- Hata yönetimi ve exception handling

### Mühendislik Kazanımları:
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

**Hazırlandı:** 27 Aralık 2025
