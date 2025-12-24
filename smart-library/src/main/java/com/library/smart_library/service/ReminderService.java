package com.library.smart_library.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.library.smart_library.model.Borrow;
import com.library.smart_library.repository.BorrowRepository;

@Service
public class ReminderService {

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private EmailService emailService;

    // ⏰ Her gün sabah 09:00'da çalışır
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkDeadlines() {
        System.out.println("⏰ Hatırlatma servisi çalıştı...");

        // Sadece iade edilmemiş (returnDate == null) kayıtları getir
        // (Bunun için Repository'e findByReturnDateNull() eklemelisin)
        List<Borrow> activeBorrows = borrowRepository.findByReturnDateNull();
        LocalDate today = LocalDate.now();

        for (Borrow borrow : activeBorrows) {
            long daysLeft = ChronoUnit.DAYS.between(today, borrow.getDueDate());

            // 📢 3 Gün veya daha az kaldıysa UYARI AT
            if (daysLeft > 0 && daysLeft <= 3) {
                String subject = "📢 Teslim Tarihi Yaklaşıyor!";
                String body = "Merhaba " + borrow.getUser().getName() + ",\n\n" +
                        "'" + borrow.getBook().getTitle() + "' kitabını teslim etmene SON " + daysLeft + " GÜN kaldı.\n"
                        +
                        "Son Tarih: " + borrow.getDueDate() + "\n\n" +
                        "Lütfen zamanında teslim etmeyi unutma.";

                emailService.sendEmail(borrow.getUser().getEmail(), subject, body);

                // Admin bildirimi (yalnızca 3 gün kala, spam'i önlemek için)
                if (daysLeft == 3) {
                    String adminBody = "Yaklaşan Teslim\nKullanıcı: " + borrow.getUser().getEmail() +
                            "\nKitap: " + borrow.getBook().getTitle() +
                            "\nKalan Gün: " + daysLeft +
                            "\nSon Tarih: " + borrow.getDueDate();
                    emailService.sendAdminNotification("[ADMIN] Yaklaşan Teslim", adminBody);
                }
            }
            // 🚨 Günü geçmişse CEZA UYARISI AT
            else if (daysLeft < 0) {
                String subject = "🚨 KİTAP GECİKTİ!";
                String body = "Merhaba,\n\n" +
                        "'" + borrow.getBook().getTitle() + "' kitabının süresi geçti!\n" +
                        "Lütfen en kısa sürede iade et.";
                emailService.sendEmail(borrow.getUser().getEmail(), subject, body);

                // Admin bildirimi (ilk gecikme gününde tek sefer)
                if (daysLeft == -1) {
                    String adminBody = "Geciken Teslim\nKullanıcı: " + borrow.getUser().getEmail() +
                            "\nKitap: " + borrow.getBook().getTitle() +
                            "\nGeciken Gün: " + Math.abs(daysLeft) +
                            "\nSon Tarih: " + borrow.getDueDate();
                    emailService.sendAdminNotification("[ADMIN] Geciken Teslim", adminBody);
                }
            }
        }
    }
}