package com.ecommerce.service;

import com.ecommerce.entity.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository, OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
    }

    /** A customer can only review a product they've actually paid for. */
    public boolean hasPurchased(User customer, Product product) {
        List<Orders> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
        return orders.stream()
                .filter(o -> o.getStatus() == Orders.OrderStatus.PAID)
                .flatMap(o -> o.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(product.getId()));
    }

    public boolean hasAlreadyReviewed(Product product, User customer) {
        return reviewRepository.existsByProductAndCustomer(product, customer);
    }

    public Review addReview(Product product, User customer, int rating, String comment) {
        if (!hasPurchased(customer, product)) {
            throw new IllegalStateException("You can only review products you've purchased.");
        }
        if (hasAlreadyReviewed(product, customer)) {
            throw new IllegalStateException("You've already reviewed this product.");
        }
        Review review = new Review();
        review.setProduct(product);
        review.setCustomer(customer);
        review.setRating(Math.max(1, Math.min(5, rating)));
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    public List<Review> findByProduct(Product product) {
        return reviewRepository.findByProductOrderByCreatedAtDesc(product);
    }

    public double averageRating(Product product) {
        List<Review> reviews = findByProduct(product);
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }
}
