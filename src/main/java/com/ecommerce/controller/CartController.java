package com.ecommerce.controller;

import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final String CART_SESSION_KEY = "cart"; // Map<productId, quantity>

    private final ProductService productService;

    public CartController(ProductService productService) {
        this.productService = productService;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                             @RequestParam(defaultValue = "1") Integer quantity,
                             HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        cart.merge(productId, quantity, Integer::sum);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId, HttpSession session) {
        getCart(session).remove(productId);
        return "redirect:/cart";
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Map<Long, Integer> cart = getCart(session);
        Map<Product, Integer> items = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productService.findById(entry.getKey());
            items.put(product, entry.getValue());
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
        }

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart";
    }
}
