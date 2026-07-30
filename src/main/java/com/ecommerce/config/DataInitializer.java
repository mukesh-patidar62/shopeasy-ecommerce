package com.ecommerce.config;

import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Creates a default admin account on first startup if one doesn't already exist.
 * Email and password are read from environment variables in production
 * (ADMIN_EMAIL, ADMIN_PASSWORD) and fall back to placeholders for local testing.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        userService.ensureAdminExists(adminEmail, adminPassword);
    }
}