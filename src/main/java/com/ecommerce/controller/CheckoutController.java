package com.ecommerce.controller;

import com.ecommerce.entity.Orders;
import com.ecommerce.entity.User;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class CheckoutController {

    private final OrderService orderService;
    private final UserService userService;

    public CheckoutController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/checkout")
    public String checkoutForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        User customer = userService.findByEmail(principal.getUsername());
        model.addAttribute("customer", customer);
        return "checkout";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/checkout")
    public String placeOrder(@AuthenticationPrincipal UserDetails principal,
                              @RequestParam String shippingName,
                              @RequestParam String shippingPhone,
                              @RequestParam String shippingAddress,
                              HttpSession session,
                              Model model) {
        User customer = userService.findByEmail(principal.getUsername());
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        Orders order = orderService.createOrderFromCart(customer, cart, shippingName, shippingPhone, shippingAddress);
        return "redirect:/payment/" + order.getId();
    }
}
