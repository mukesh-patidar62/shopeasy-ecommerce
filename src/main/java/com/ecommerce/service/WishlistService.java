package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.entity.WishlistItem;
import com.ecommerce.repository.WishlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    public void add(User customer, Product product) {
        if (!wishlistRepository.existsByCustomerAndProduct(customer, product)) {
            WishlistItem item = new WishlistItem();
            item.setCustomer(customer);
            item.setProduct(product);
            wishlistRepository.save(item);
        }
    }

    public void remove(User customer, Product product) {
        wishlistRepository.findByCustomerAndProduct(customer, product)
                .ifPresent(wishlistRepository::delete);
    }

    public boolean isInWishlist(User customer, Product product) {
        return wishlistRepository.existsByCustomerAndProduct(customer, product);
    }

    public List<WishlistItem> findByCustomer(User customer) {
        return wishlistRepository.findByCustomerOrderByAddedAtDesc(customer);
    }
}