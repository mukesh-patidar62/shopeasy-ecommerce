package com.ecommerce.controller;

import com.ecommerce.entity.User;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/orders")
    public String myOrders(@AuthenticationPrincipal UserDetails principal, Model model) {
        User customer = userService.findByEmail(principal.getUsername());
        model.addAttribute("orders", orderService.findByCustomer(customer));
        return "orders";
    }
}
