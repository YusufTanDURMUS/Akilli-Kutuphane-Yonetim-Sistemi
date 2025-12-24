package com.library.smart_library.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library.smart_library.model.Borrow;
import com.library.smart_library.model.User;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    // Bir kullanıcının aldığı tüm kitapları bul
    List<Borrow> findByUser(User user);

    // İade tarihi NULL olanları (yani hala öğrencide olanları) getir
    List<Borrow> findByReturnDateNull();
}