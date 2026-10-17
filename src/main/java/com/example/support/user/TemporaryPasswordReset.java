package com.example.support.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
    
@Component
@Profile("reset-password")
public class TemporaryPasswordReset implements CommandLineRunner {

    private final UserService userService;

    public TemporaryPasswordReset(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        String username = System.getenv("RESET_USERNAME");
        String password = System.getenv("RESET_PASSWORD");

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Set RESET_USERNAME and RESET_PASSWORD first.");
        }

        userService.resetPassword(username, password);
        System.out.println("Password reset completed for the test account.");
    }
}
