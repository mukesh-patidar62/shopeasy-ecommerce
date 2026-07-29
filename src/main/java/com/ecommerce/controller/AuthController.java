package com.ecommerce.controller;

import com.ecommerce.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam(required = false) String phone,
                            @RequestParam(required = false) String address,
                            Model model) {
        try {
            userService.registerCustomer(fullName, email, password, phone, address);
            model.addAttribute("success", "Account created! Please log in.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }
}
