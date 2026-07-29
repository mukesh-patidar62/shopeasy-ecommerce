package com.ecommerce.service;

import com.ecommerce.entity.Orders;
import com.ecommerce.entity.Payment;
import com.ecommerce.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /** Creates a Razorpay order for the given amount (in rupees) and links it to our Order. */
    public JSONObject createRazorpayOrder(Orders order) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        // Razorpay expects the amount in paise (smallest currency unit)
        int amountInPaise = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put("receipt", "order_rcpt_" + order.getId());

        com.razorpay.Order razorpayOrder = client.orders.create(options);

        order.setRazorpayOrderId(razorpayOrder.get("id"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(Payment.PaymentStatus.CREATED);
        paymentRepository.save(payment);

        JSONObject response = new JSONObject();
        response.put("razorpayOrderId", razorpayOrder.get("id").toString());
        response.put("amount", amountInPaise);
        response.put("currency", "INR");
        response.put("keyId", keyId);
        return response;
    }

    /** Verifies the signature Razorpay sends back after checkout completes. */
    public boolean verifyAndRecordPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(attributes, keySecret);

            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId).orElse(null);
            if (payment != null) {
                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setRazorpaySignature(razorpaySignature);
                payment.setStatus(isValid ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }
}
