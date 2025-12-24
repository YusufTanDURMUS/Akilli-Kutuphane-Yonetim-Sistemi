package com.library.smart_library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String fromEmail;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    // 1. GENEL MAİL GÖNDERME METODU (Altyapı)
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String sender = (fromEmail == null || fromEmail.isBlank())
                    ? (mailUsername == null || mailUsername.isBlank() ? "no-reply@localhost" : mailUsername)
                    : fromEmail;
            message.setFrom(sender);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            System.out.println("Mail başarıyla gönderildi: " + to);
        } catch (Exception e) {
            System.err.println("Mail gönderme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 2. ÖZEL HOŞ GELDİN MAİLİ METODU (Senin aradığın bu!) 👇
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Aramıza Hoş Geldin! 🚀";
        String body = "Merhaba " + name + ",\n\n" +
                "Sanal Kütüphane üyeliğin başarıyla oluşturuldu.\n" +
                "Hemen giriş yapıp dilediğin kitabı ödünç alabilirsin.\n\n" +
                "İyi okumalar dileriz,\nSmart Library Ekibi";

        // Yukarıdaki genel metodu kullanarak gönderiyoruz
        sendEmail(to, subject, body);
    }

    // 3. ŞİFRE SIFIRLAMA MAİLİ METODU
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        String subject = "Şifre Sıfırlama Talebi 🔐";
        String body = "Merhaba " + name + ",\n\n" +
                "Şifrenizi sıfırlamak için aşağıdaki linke tıklayın:\n\n" +
                resetLink + "\n\n" +
                "Bu link 24 saat geçerlidir.\n" +
                "Eğer bu talebi siz yapmadıysanız, lütfen bu maili dikkate almayın.\n\n" +
                "Saygılarımızla,\nSmart Library Ekibi";

        sendEmail(to, subject, body);
    }

    // 4. E-POSTA DOĞRULAMA MAİLİ
    public void sendVerificationEmail(String to, String name, String verifyLink) {
        String subject = "E-Posta Doğrulama ✅";
        String body = "Merhaba " + name + ",\n\n" +
                "Üyeliğini aktifleştirmek için aşağıdaki doğrulama bağlantısına tıkla:\n\n" +
                verifyLink + "\n\n" +
                "Bağlantı bir süre sonra geçersiz olacaktır.\n\n" +
                "Smart Library Ekibi";
        sendEmail(to, subject, body);
    }

    // 5. İADE MAİLİ
    public void sendReturnEmail(String to, String name, String bookTitle, boolean late, long daysOverdue, long fine) {
        String subject = late ? "İade Edildi (Gecikme/Ceza Var)" : "İade Edildi ✅";
        String body = late
                ? ("Merhaba " + name + ",\n\n'" + bookTitle + "' kitabını iade ettiniz, ancak \n" +
                        "Gecikme: " + daysOverdue + " gün\n" +
                        "Ceza: " + fine + " TL\n\n" +
                        "Lütfen bir sonraki teslim tarihinde daha dikkatli olun.\n\nSmart Library")
                : ("Merhaba " + name + ",\n\n'" + bookTitle
                        + "' kitabını zamanında iade ettiğiniz için teşekkürler!\n\nSmart Library");
        sendEmail(to, subject, body);
    }

    // 6. CEZA MAİLİ (iade anında ayrı bildirmek istenirse)
    public void sendPenaltyEmail(String to, String name, String bookTitle, long daysOverdue, long fine) {
        String subject = "Ceza Bilgilendirme 💸";
        String body = "Merhaba " + name + ",\n\n'" + bookTitle + "' kitabını geç iade ettiğiniz için\n" +
                "Gecikme: " + daysOverdue + " gün\n" +
                "Ceza: " + fine + " TL\n\n" +
                "Smart Library";
        sendEmail(to, subject, body);
    }

    // 7. ADMIN BİLDİRİMİ
    public void sendAdminNotification(String subject, String body) {
        if (adminEmail != null && !adminEmail.isBlank()) {
            sendEmail(adminEmail, subject, body);
        }
    }
}