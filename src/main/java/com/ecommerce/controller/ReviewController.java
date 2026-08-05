package com.ecommerce.controller;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.ReviewService;
import com.ecommerce.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, ProductService productService, UserService userService) {
        this.reviewService = reviewService;
        this.productService = productService;
        this.userService = userService;
    }

    @PostMapping("/products/{id}/reviews")
    public String addReview(@PathVariable Long id,
                            @RequestParam int rating,
                            @RequestParam(required = false) String comment,
                            @AuthenticationPrincipal UserDetails principal,
                            Model model) {
        Product product = productService.findById(id);
        User customer = userService.findByEmail(principal.getUsername());

        try {
            reviewService.addReview(product, customer, rating, comment);
        } catch (IllegalStateException e) {
            // Re-render the product page with an error rather than a raw 500
            model.addAttribute("product", product);
            model.addAttribute("reviews", reviewService.findByProduct(product));
            model.addAttribute("averageRating", reviewService.averageRating(product));
            model.addAttribute("reviewError", e.getMessage());
            return "product-detail";
        }
        return "redirect:/products/" + id;
    }
}
