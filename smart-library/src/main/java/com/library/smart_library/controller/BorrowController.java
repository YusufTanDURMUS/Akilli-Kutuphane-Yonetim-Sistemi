package com.library.smart_library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired; // 👈 BU EKLENDİ
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.smart_library.model.Borrow;
import com.library.smart_library.repository.BorrowRepository;
import com.library.smart_library.service.BorrowService;

@RestController
@RequestMapping("/api/v1/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private BorrowRepository borrowRepository; // 👈 BU EKLENDİ (Artık findAll() çalışacak)

    // Ödünç Al (SÜRE SEÇİMLİ) -> ?days=7
    @PostMapping("/{bookId}")
    public String borrowBook(@PathVariable Long bookId, @RequestParam int days) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return borrowService.borrowBook(email, bookId, days);
    }

    // İade Et
    @PostMapping("/return/{borrowId}")
    public String returnBook(@PathVariable Long borrowId) {
        return borrowService.returnBook(borrowId);
    }

    // Aldıklarımı Gör
    @GetMapping("/my-books")
    public List<Borrow> getMyBooks() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return borrowService.getMyBooks(email);
    }

    // 👇 TÜM KAYITLARI GÖR (DEBUG İÇİN)
    @GetMapping("/all")
    public List<Borrow> getAllBorrows() {
        return borrowRepository.findAll();
    }
}