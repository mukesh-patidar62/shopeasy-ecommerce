package com.ecommerce.controller;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.entity.WishlistItem;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.UserService;
import com.ecommerce.service.WishlistService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final ProductService productService;
    private final UserService userService;

    public WishlistController(WishlistService wishlistService, ProductService productService, UserService userService) {
        this.wishlistService = wishlistService;
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping
    public String viewWishlist(@AuthenticationPrincipal UserDetails principal, Model model) {
        User customer = userService.findByEmail(principal.getUsername());
        List<WishlistItem> items = wishlistService.findByCustomer(customer);
        model.addAttribute("items", items);
        return "wishlist";
    }

    @PostMapping("/add/{productId}")
    public String addToWishlist(@PathVariable Long productId,
                                @AuthenticationPrincipal UserDetails principal) {
        User customer = userService.findByEmail(principal.getUsername());
        Product product = productService.findById(productId);
        wishlistService.add(customer, product);
        return "redirect:/products/" + productId;
    }

    @PostMapping("/remove/{productId}")
    public String removeFromWishlist(@PathVariable Long productId,
                                     @AuthenticationPrincipal UserDetails principal) {
        User customer = userService.findByEmail(principal.getUsername());
        Product product = productService.findById(productId);
        wishlistService.remove(customer, product);
        return "redirect:/wishlist";
    }
}
