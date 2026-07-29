package com.ecommerce.config;

import com.ecommerce.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Creates a default admin account on first startup so you have something to
 * log in with immediately: admin@shop.com / admin123
 * Change the password after first login in a real deployment.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        userService.ensureAdminExists("admin@shop.com", "admin123");
    }
}
