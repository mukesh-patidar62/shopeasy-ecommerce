package com.ecommerce.controller;

import com.ecommerce.service.OrderService;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Razorpay calls this endpoint directly from its own servers (not the customer's
 * browser) whenever a payment event happens. This is a safety net: our normal
 * client-side flow (payment.html -> /api/payment/verify) already marks orders
 * paid, but if the customer closes their browser right after paying and before
 * that request completes, the order would otherwise stay stuck as PENDING even
 * though the payment genuinely succeeded. This webhook catches that case.
 *
 * Setup: in your Razorpay Dashboard -> Settings -> Webhooks, add a webhook
 * pointing at https://your-domain/api/webhooks/razorpay, subscribed to at
 * least the "payment.captured" event, and set a Webhook Secret (different
 * from your API key/secret) - put that value in RAZORPAY_WEBHOOK_SECRET.
 */
@RestController
class WebhookController {

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private final OrderService orderService;

    public WebhookController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/webhooks/razorpay")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        try {
            if (signature == null || !Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
                return ResponseEntity.status(400).body("invalid signature");
            }

            JSONObject json = new JSONObject(payload);
            String event = json.optString("event", "");

            if ("payment.captured".equals(event) || "order.paid".equals(event)) {
                String razorpayOrderId = json.getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity")
                        .getString("order_id");
                orderService.markPaidFromWebhook(razorpayOrderId);
            }

            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            e.printStackTrace();
            // Return 200 anyway so Razorpay doesn't endlessly retry on a parsing
            // quirk for an event type we don't care about - we only act on
            // events we explicitly recognize above.
            return ResponseEntity.ok("ignored");
        }
    }
}
