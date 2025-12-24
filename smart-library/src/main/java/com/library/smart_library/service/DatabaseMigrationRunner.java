package com.library.smart_library.service;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner implements CommandLineRunner {

    private final DataSource dataSource;

    public DatabaseMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        // Güvenli şekilde role sütununu genişletmeye çalışıyoruz
        String sql = "ALTER TABLE users MODIFY role VARCHAR(20);";
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
            System.out.println("DB Migration: 'users.role' column modified to VARCHAR(20) if needed.");
        } catch (Exception e) {
            System.out.println("DB Migration skipped or failed (may already be applied): " + e.getMessage());
        }
    }
}
