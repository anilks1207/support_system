package com.example.support.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(
            String username,
            String email,
            String displayName,
            String rawPassword) {

        username = username == null ? "" : username.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        displayName = displayName == null ? "" : displayName.trim();

        if (username.isBlank() || email.isBlank()
                || displayName.isBlank() || rawPassword == null) {
            throw new IllegalArgumentException(
                    "All fields are required.");
        }

        if (rawPassword.length() < 12) {
            throw new IllegalArgumentException(
                    "Password must contain at least 12 characters.");
        }

        if (username.length() > 50
                || email.length() > 255
                || displayName.length() > 100) {
            throw new IllegalArgumentException(
                    "One or more fields exceed the allowed length.");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username is already registered.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Transactional
    public void resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found."));

        if (newPassword == null || newPassword.length() < 12) {
            throw new IllegalArgumentException(
                    "Password must contain at least 12 characters.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    @Transactional
    public User registerUserByAdmin(
            String username,
            String email,
            String displayName,
            String rawPassword,
            String role) {

        // Only an authenticated Admin can create users through this method.
        var authentication =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();

        boolean isAdmin = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                                || authority.getAuthority().equals("ADMIN"));

        if (!isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only administrators can create users with assigned roles.");
        }

        if (role == null) {
            throw new IllegalArgumentException("Please select a role.");
        }

        role = role.trim().toUpperCase(java.util.Locale.ROOT);

        if (!java.util.Set.of(
                "USER", "SUPPORT", "MAINTENANCE", "ADMIN"
        ).contains(role)) {
            throw new IllegalArgumentException("Invalid user role.");
        }

        User user = registerUser(
                username, email, displayName, rawPassword);

        user.setRole(role);

        return userRepository.save(user);
    }

}

