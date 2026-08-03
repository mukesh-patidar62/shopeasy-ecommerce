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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ProductService productService;
    private final ReviewService reviewService;
    private final UserService userService;

    public HomeController(ProductService productService, ReviewService reviewService, UserService userService) {
        this.productService = productService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String search,
                       Model model) {
        List<Product> allProducts = productService.findAll();

        List<String> categories = allProducts.stream()
                .map(Product::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<Product> visible = allProducts;
        if (category != null && !category.isBlank()) {
            visible = visible.stream()
                    .filter(p -> category.equalsIgnoreCase(p.getCategory()))
                    .collect(Collectors.toList());
        }
        if (search != null && !search.isBlank()) {
            String needle = search.toLowerCase();
            visible = visible.stream()
                    .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(needle))
                    .collect(Collectors.toList());
        }

        model.addAttribute("products", visible);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchQuery", search);
        return "home";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails principal,
                                Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewService.findByProduct(product));
        model.addAttribute("averageRating", reviewService.averageRating(product));

        boolean canReview = false;
        if (principal != null) {
            User customer = userService.findByEmail(principal.getUsername());
            canReview = reviewService.hasPurchased(customer, product)
                    && !reviewService.hasAlreadyReviewed(product, customer);
        }
        model.addAttribute("canReview", canReview);

        return "product-detail";
    }
}
