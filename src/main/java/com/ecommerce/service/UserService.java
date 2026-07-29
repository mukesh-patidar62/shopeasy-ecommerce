package com.ecommerce.service;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerCustomer(String fullName, String email, String rawPassword, String phone, String address) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhone(phone);
        user.setAddress(address);
        user.setRole(User.Role.CUSTOMER);
        return userRepository.save(user);
    }

    /** Used once at startup to guarantee there's always an admin account to log in with. */
    public User ensureAdminExists(String email, String rawPassword) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User admin = new User();
            admin.setFullName("Administrator");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(rawPassword));
            admin.setRole(User.Role.ADMIN);
            return userRepository.save(admin);
        });
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
