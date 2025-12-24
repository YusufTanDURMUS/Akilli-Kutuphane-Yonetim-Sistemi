package com.library.smart_library.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Tüm Controller'ları dinleyen bir "Dinleyici"
public class GlobalExceptionHandler {

    // Validasyon hatası (MethodArgumentNotValidException) olursa burası çalışır
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Hangi alanlarda hata varsa onları topluyoruz
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        // 400 Bad Request koduyla temiz listeyi dönüyoruz
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Genel istisnaları yakalayıp basit bir mesaj döndürüyoruz (debug amaçlı)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        Map<String, String> err = new HashMap<>();
        err.put("error", ex.getClass().getSimpleName());
        err.put("message", ex.getMessage() != null ? ex.getMessage() : "(no message)");
        ex.printStackTrace();
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}