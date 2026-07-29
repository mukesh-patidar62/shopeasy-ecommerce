package com.ecommerce.repository;

import com.ecommerce.entity.Orders;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByCustomerOrderByCreatedAtDesc(User customer);
    Optional<Orders> findByRazorpayOrderId(String razorpayOrderId);
    List<Orders> findAllByOrderByCreatedAtDesc();
}
