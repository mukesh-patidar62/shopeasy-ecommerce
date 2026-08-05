package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByCustomerOrderByAddedAtDesc(User customer);
    Optional<WishlistItem> findByCustomerAndProduct(User customer, Product product);
    boolean existsByCustomerAndProduct(User customer, Product product);
}
