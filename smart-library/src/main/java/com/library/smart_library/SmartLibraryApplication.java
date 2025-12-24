package com.library.smart_library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // BU SATIR ÇOK ÖNEMLİ! ZAMANLAYICIYI AÇAR.
public class SmartLibraryApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartLibraryApplication.class, args);
	}
}