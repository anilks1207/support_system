package com.example.support;

import org.flywaydb.core.Flyway;

public class FlywayRepairOnce {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/support_system";
        String user = "postgres";
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "DB_PASSWORD environment variable is not set."
            );
        }

        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .load();

        flyway.repair();

        System.out.println("Flyway repair completed.");
    }
}


