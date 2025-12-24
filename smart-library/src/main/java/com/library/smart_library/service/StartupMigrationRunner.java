package com.library.smart_library.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class StartupMigrationRunner implements CommandLineRunner {

    private final DataSource dataSource;

    public StartupMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1) Try programmatic Flyway migrate
        try {
            System.out.println("Attempting Flyway migrate...");
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .load();
            flyway.migrate();
            System.out.println("Flyway migrate completed.");
            return;
        } catch (FlywayException fe) {
            System.out.println("Flyway migrate failed or unsupported: " + fe.getMessage());
        } catch (Throwable t) {
            System.out.println("Unexpected Flyway error: " + t.getMessage());
        }

        // 2) Fallback: Execute SQL files under classpath:db/migration manually
        System.out.println("Falling back to manual SQL migration from classpath:db/migration");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:db/migration/*.sql");
        List<String> sqlStatements = new ArrayList<>();

        for (Resource r : resources) {
            try (InputStream is = r.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                // crude split by semicolon
                String[] parts = sb.toString().split(";\n");
                for (String p : parts) {
                    String s = p.trim();
                    if (!s.isEmpty())
                        sqlStatements.add(s);
                }
            } catch (Exception e) {
                System.out.println("Failed reading resource " + r.getFilename() + ": " + e.getMessage());
            }
        }

        if (!sqlStatements.isEmpty()) {
            try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
                for (String s : sqlStatements) {
                    try {
                        st.execute(s);
                        System.out.println("Executed SQL statement (len=" + s.length() + ")");
                    } catch (Exception e) {
                        System.out.println("SQL exec failed: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.out.println("Manual migration failed: " + e.getMessage());
            }
        } else {
            System.out.println("No SQL migration files found in classpath:db/migration");
        }
    }
}
