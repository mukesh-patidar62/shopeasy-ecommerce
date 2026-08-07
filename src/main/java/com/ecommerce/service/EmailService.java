package com.ecommerce.service;

import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Orders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** Sends a plain-text order confirmation. Failures are logged, never block the checkout flow. */
    public void sendOrderConfirmation(Orders order) {
        if (order.getCustomer() == null || order.getCustomer().getEmail() == null) return;

        try {
            StringBuilder body = new StringBuilder();
            body.append("Hi ").append(order.getShippingName()).append(",\n\n");
            body.append("Thanks for your order! Here's a summary:\n\n");
            body.append("Order #").append(order.getId()).append("\n");
            body.append("Status: ").append(order.getStatus()).append("\n\n");

            for (OrderItem item : order.getItems()) {
                body.append("- ").append(item.getProduct().getName())
                        .append(" x ").append(item.getQuantity())
                        .append(" @ ₹").append(item.getPriceAtPurchase())
                        .append("\n");
            }

            body.append("\nTotal: ₹").append(order.getTotalAmount()).append("\n\n");
            body.append("Delivering to:\n").append(order.getShippingAddress()).append("\n\n");
            body.append("Thanks for shopping with ShopEasy!");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(order.getCustomer().getEmail());
            message.setSubject("ShopEasy - Order #" + order.getId() + " confirmed");
            message.setText(body.toString());

            mailSender.send(message);
        } catch (Exception e) {
            // Never let a failed email break the actual checkout/payment flow
            e.printStackTrace();
        }
    }
}
