package com.ecommerce.controller;
import com.ecommerce.service.AiDescriptionService;
import java.util.Map;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AiDescriptionService aiDescriptionService;

    // add aiDescriptionService to the existing constructor parameters and assignment
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;

    public AdminController(AiDescriptionService aiDescriptionService, ProductService productService, UserService userService, OrderService orderService) {
        this.aiDescriptionService = aiDescriptionService;
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
    }
    @PostMapping("/products/generate-description")
    @ResponseBody
    public Map<String, String> generateDescription(@RequestParam String name,
                                                   @RequestParam(required = false) String category) {
        String description = aiDescriptionService.generateDescription(name, category);
        return Map.of("description", description);
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateStatus(id, com.ecommerce.entity.Orders.OrderStatus.valueOf(status));
        return "redirect:/admin/orders";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/dashboard";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @AuthenticationPrincipal UserDetails principal) throws Exception {
        User admin = userService.findByEmail(principal.getUsername());

        // preserve existing image if editing and no new file chosen
        if ((imageFile == null || imageFile.isEmpty()) && product.getId() != null) {
            Product existing = productService.findById(product.getId());
            product.setImageUrl(existing.getImageUrl());
            product.setCreatedBy(existing.getCreatedBy());
        }

        productService.save(product, imageFile, admin);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/orders")
    public String viewOrders(Model model) {
        model.addAttribute("orders", orderService.findAll());
        return "admin/orders";
    }
}
