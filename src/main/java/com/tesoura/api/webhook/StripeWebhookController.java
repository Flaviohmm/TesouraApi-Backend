package com.tesoura.api.webhook;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeWebhookService webhookService;

    public StripeWebhookController(StripeWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "Stripe-Signature", required = false) String signature,
            @RequestBody String rawBody
    ) {
        if (signature == null) {
            log.warn("Rejected Stripe webhook with missing signature header");
            return ResponseEntity.status(401).build();
        }
        Event event;
        try {
            event = webhookService.verify(rawBody, signature);
        } catch (SignatureVerificationException ex) {
            log.warn("Rejected Stripe webhook with invalid signature");
            return ResponseEntity.status(401).build();
        }

        try {
            webhookService.process(event);
        } catch (Exception ex) {
            log.error("Failed to process Stripe webhook", ex);
        }
        return ResponseEntity.ok().build();
    }
}
