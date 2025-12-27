# Proje Raporu Kullanım Talimatları

## Ekran Görüntülerini Kaydetme

Lütfen aşağıdaki ekran görüntülerini `screenshots` klasörüne kaydedin:

### 1. E-posta Bildirimleri
- **email-welcome.png**: Hoş geldin e-postası
- **email-verification.png**: E-posta doğrulama bildirimi
- **email-password-reset.png**: Şifre sıfırlama e-postası

### 2. Web Sayfaları
- **password-reset-page.png**: Şifre sıfırlama ekranı

### 3. API Testleri
- **postman-403-forbidden.png**: Yetkisiz erişim testi

## HTML Raporunu Word/PDF'e Dönüştürme

### Yöntem 1: Web Tarayıcısı ile (Önerilen)
1. `PROJE_RAPORU.html` dosyasını herhangi bir web tarayıcısında açın
2. Tarayıcıdan **Yazdır** (Ctrl+P) seçeneğini açın
3. **Hedef** olarak "PDF olarak kaydet" veya "Microsoft Print to PDF" seçin
4. **Kaydet** butonuna tıklayın

### Yöntem 2: Microsoft Word ile
1. Microsoft Word'ü açın
2. **Dosya > Aç** menüsünden `PROJE_RAPORU.html` dosyasını açın
3. **Dosya > Farklı Kaydet** seçeneğini kullanarak:
   - PDF formatında kaydedin veya
   - DOCX formatında kaydedin

### Yöntem 3: Microsoft Edge ile (Windows)
1. `PROJE_RAPORU.html` dosyasına sağ tıklayın
2. **Birlikte Aç > Microsoft Edge** seçin
3. Sağ üst köşeden **Yazdır** ikonuna tıklayın
4. **PDF olarak kaydet** seçeneğini kullanın

## Dosya Yapısı

```
smart.library/
├── PROJE_RAPORU.md              # Markdown formatında rapor
├── PROJE_RAPORU.html            # HTML formatında rapor (yazdırılabilir)
├── EKRAN_GORUNTU_TALIMATLARI.md # Bu talimatlar
├── README.md                     # Proje ana README'si
├── VERITABANI_SETUP.sql         # Veritabanı kurulum scripti
└── screenshots/                  # Ekran görüntüleri klasörü
    ├── email-welcome.png
    ├── email-verification.png
    ├── email-password-reset.png
    ├── password-reset-page.png
    └── postman-403-forbidden.png
```

## Önemli Notlar

- HTML dosyası tüm stillendirmeleri içerir ve yazdırma için optimize edilmiştir
- Ekran görüntülerini kaydettikten sonra HTML dosyasını açtığınızda tüm resimler görünecektir
- PDF oluştururken "Arka Plan Grafikleri" seçeneğini aktifleştirin
- Sayfa kenar boşlukları otomatik olarak ayarlanmıştır (2.5cm)

## Sorun Giderme

Eğer HTML dosyasını açarken resimler görünmüyorsa:
1. Ekran görüntülerinin `screenshots` klasöründe doğru isimlerle kaydedildiğinden emin olun
2. Dosya yollarını kontrol edin (büyük/küçük harf duyarlı)
3. Tarayıcının önbelleğini temizleyin (Ctrl+F5)

## İletişim

Sorularınız için: yusuftandurmus@gmail.com
GitHub: https://github.com/YusufTanDURMUS/Akilli-Kutuphane-Yonetim-Sistemi
