package com.ecommerce.service;

import com.ecommerce.entity.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
    }

    /**
     * cartItems: map of productId -> quantity
     */
    public Orders createOrderFromCart(User customer, Map<Long, Integer> cartItems,
                                      String shippingName, String shippingPhone, String shippingAddress,
                                      Orders.PaymentMethod paymentMethod) {
        Orders order = new Orders();
        order.setCustomer(customer);
        order.setShippingName(shippingName);
        order.setShippingPhone(shippingPhone);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + entry.getKey()));
            int qty = entry.getValue();

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(qty);
            item.setPriceAtPurchase(product.getPrice());
            order.getItems().add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }
        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    /**
     * Places a Cash on Delivery order: skips Razorpay entirely, confirms the order
     * immediately, and reduces stock right away since the customer has committed
     * to the purchase (payment happens physically on delivery instead).
     */
    public Orders placeCodOrder(User customer, Map<Long, Integer> cartItems,
                                String shippingName, String shippingPhone, String shippingAddress) {
        Orders order = createOrderFromCart(customer, cartItems, shippingName, shippingPhone, shippingAddress,
                Orders.PaymentMethod.COD);
        order.setStatus(Orders.OrderStatus.CONFIRMED);
        orderRepository.save(order);
        reduceStock(order);
        emailService.sendOrderConfirmation(order);
        return order;
    }

    public Orders save(Orders order) {
        return orderRepository.save(order);
    }

    public Orders findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Order not found: " + id));
    }

    public List<Orders> findByCustomer(User customer) {
        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    public List<Orders> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /** Reduce stock once payment is confirmed. */
    public void reduceStock(Orders order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int newStock = Math.max(0, product.getStock() - item.getQuantity());
            product.setStock(newStock);
            productRepository.save(product);
        }
    }

    /** Admin manually advances an order's status (e.g. Paid -> Shipped -> Delivered). */
    public Orders updateStatus(Long orderId, Orders.OrderStatus newStatus) {
        Orders order = findById(orderId);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Called by the Razorpay webhook as a safety net: if the client-side payment
     * confirmation never reached our server (e.g. browser closed right after paying),
     * this marks the order paid from the server-to-server webhook instead.
     * Idempotent - safe to call even if the order is already PAID.
     */
    public void markPaidFromWebhook(String razorpayOrderId) {
        orderRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(order -> {
            if (order.getStatus() == Orders.OrderStatus.PENDING) {
                order.setStatus(Orders.OrderStatus.PAID);
                orderRepository.save(order);
                reduceStock(order);
                emailService.sendOrderConfirmation(order);
            }
        });
    }
}
