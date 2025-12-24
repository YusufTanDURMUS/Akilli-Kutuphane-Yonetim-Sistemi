
package com.library.smart_library.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Bu sınıfın web isteklerini karşılayacağını belirtir
public class HelloController {

    @GetMapping("/merhaba") // Tarayıcıda /merhaba adresine gidilince bu çalışır
    public String selamla() {
        return "Smart Library Sistemine Hoş Geldiniz! 📚";
    }
}
