package com.library.smart_library.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.smart_library.model.Book;
import com.library.smart_library.model.Borrow;
import com.library.smart_library.model.User;
import com.library.smart_library.repository.BookRepository;
import com.library.smart_library.repository.BorrowRepository;
import com.library.smart_library.repository.UserRepository;

@Service
public class BorrowService {

    @Autowired
    private BorrowRepository borrowRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService; // Postacımız burada hazır bekliyor

    // --- KİTAP ÖDÜNÇ AL ---
    @Transactional
    public String borrowBook(String userEmail, long bookId, int days) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı"));

        if (book.getStock() <= 0) {
            throw new RuntimeException("Stokta kitap kalmadı!");
        }

        // Stok düş (kilitli satırda)
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);

        // Tarih Hesaplama
        LocalDate today = LocalDate.now();
        LocalDate calculatedDueDate = today.plusDays(days); // Hesaplanan Son Teslim Tarihi

        // Yeni Constructor ile kayıt
        Borrow borrow = new Borrow(user, book, today, calculatedDueDate);
        borrowRepository.save(borrow);

        // EKSİK OLAN KISIM BURASIYDI: MAİL GÖNDERME
        try {
            String mailIcerigi = "Merhaba,\n\n" +
                    "'" + book.getTitle() + "' kitabını kütüphanemizden ödünç aldın.\n" +
                    "Son Teslim Tarihi: " + calculatedDueDate + "\n\n" +
                    "Keyifli okumalar dileriz!\nSmart Library Ekibi";

            emailService.sendEmail(userEmail, "Kitap Ödünç Alındı 📚", mailIcerigi);

            // Admin bildirimi
            String adminBody = "Ödünç Alma\nKullanıcı: " + user.getEmail() +
                    "\nKitap: " + book.getTitle() +
                    "\nSon Tarih: " + calculatedDueDate;
            emailService.sendAdminNotification("[ADMIN] Kitap Ödünç Alındı", adminBody);
        } catch (Exception e) {
            System.out.println("Mail atılamadı ama işlem devam ediyor: " + e.getMessage());
        }
        // MAİL KISMI BİTTİ

        return "Kitap alındı! Son teslim tarihiniz: " + calculatedDueDate + " (" + days + " Gün)";
    }

    // --- KİTAP İADE ET ---
    @Transactional
    public String returnBook(long borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Kayıt bulunamadı"));

        if (borrow.getReturnDate() != null) {
            return "Bu kitap zaten iade edilmiş.";
        }

        // İade tarihini bugün yap
        borrow.setReturnDate(LocalDate.now());

        // Stoğu artır
        Book book = borrow.getBook();
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        // CEZA HESAPLAMA
        long daysOverdue = ChronoUnit.DAYS.between(borrow.getDueDate(), LocalDate.now());

        borrowRepository.save(borrow);

        // Kullanıcı ve kitap bilgileri
        User user = borrow.getUser();
        Book returnedBook = borrow.getBook();

        if (daysOverdue > 0) {
            long dailyFee = 10; // Günlük 10 TL
            long totalFine = daysOverdue * dailyFee;

            // Ceza/iade maili gönder
            try {
                emailService.sendReturnEmail(user.getEmail(), user.getName(), returnedBook.getTitle(), true,
                        daysOverdue, totalFine);
                emailService.sendPenaltyEmail(user.getEmail(), user.getName(), returnedBook.getTitle(), daysOverdue,
                        totalFine);

                // Admin bildirimi
                String adminBody = "Gecikmeli İade\nKullanıcı: " + user.getEmail() +
                        "\nKitap: " + returnedBook.getTitle() +
                        "\nGecikme: " + daysOverdue + " gün\nCeza: " + totalFine + " TL";
                emailService.sendAdminNotification("[ADMIN] Gecikmeli İade ve Ceza", adminBody);
            } catch (Exception e) {
                System.out.println("Ceza maili gönderilemedi: " + e.getMessage());
            }

            return "KİTAP GECİKTİ! 🚨\n" +
                    "Son Teslim: " + borrow.getDueDate() + "\n" +
                    "Gecikme: " + daysOverdue + " gün\n" +
                    "ÖDEMENİZ GEREKEN CEZA: " + totalFine + " TL";
        } else {
            // Zamanında iade maili
            try {
                emailService.sendReturnEmail(user.getEmail(), user.getName(), returnedBook.getTitle(), false, 0, 0);

                // Admin bildirimi
                String adminBody = "Zamanında İade\nKullanıcı: " + user.getEmail() +
                        "\nKitap: " + returnedBook.getTitle();
                emailService.sendAdminNotification("[ADMIN] Zamanında İade", adminBody);
            } catch (Exception e) {
                System.out.println("İade maili gönderilemedi: " + e.getMessage());
            }

            return "Teşekkürler, zamanında teslim ettiniz!";
        }
    }

    public List<Borrow> getMyBooks(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        return borrowRepository.findByUser(user);
    }
}