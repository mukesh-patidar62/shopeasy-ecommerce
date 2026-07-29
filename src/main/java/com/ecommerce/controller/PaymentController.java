package com.ecommerce.controller;

import com.ecommerce.entity.Orders;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    /** Shows the payment page which loads Razorpay's Checkout.js widget. */
    @GetMapping("/payment/{orderId}")
    public String paymentPage(@PathVariable Long orderId, Model model) {
        Orders order = orderService.findById(orderId);
        model.addAttribute("order", order);
        return "payment";
    }

    /** Called by the frontend JS to create a Razorpay order right before opening checkout. */
    @PostMapping("/api/payment/create-order/{orderId}")
    @ResponseBody
    public JSONObject createOrder(@PathVariable Long orderId) throws Exception {
        Orders order = orderService.findById(orderId);
        JSONObject result = paymentService.createRazorpayOrder(order);
        orderService.save(order); // persist the razorpayOrderId we just set on it
        return result;
    }

    /** Called by the frontend JS after Razorpay's checkout succeeds, to verify + finalize. */
    @PostMapping("/api/payment/verify")
    @ResponseBody
    public Map<String, Object> verifyPayment(@RequestBody Map<String, String> body, HttpSession session) {
        String razorpayOrderId = body.get("razorpay_order_id");
        String razorpayPaymentId = body.get("razorpay_payment_id");
        String razorpaySignature = body.get("razorpay_signature");
        Long orderId = Long.valueOf(body.get("orderId"));

        boolean valid = paymentService.verifyAndRecordPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);

        Orders order = orderService.findById(orderId);
        if (valid) {
            order.setStatus(Orders.OrderStatus.PAID);
            orderService.save(order);
            orderService.reduceStock(order);
            session.removeAttribute("cart"); // empty the cart now that payment succeeded
        } else {
            order.setStatus(Orders.OrderStatus.FAILED);
            orderService.save(order);
        }

        return Map.of("success", valid, "orderId", orderId);
    }

    @GetMapping("/order-success/{orderId}")
    public String orderSuccess(@PathVariable Long orderId, Model model) {
        model.addAttribute("order", orderService.findById(orderId));
        return "order-success";
    }
}
