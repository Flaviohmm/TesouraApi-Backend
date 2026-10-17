package com.tesoura.api.payment;

import com.tesoura.api.payment.dto.PaymentCheckoutDTO;
import com.tesoura.api.payment.dto.PaymentStatusDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/salons/{slug}/appointments/{appointmentId}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentCheckoutDTO create(@PathVariable String slug, @PathVariable UUID appointmentId) {
        return paymentService.createCheckout(slug, appointmentId);
    }

    @GetMapping("/status")
    public PaymentStatusDTO status(@PathVariable String slug, @PathVariable UUID appointmentId) {
        return paymentService.getStatus(appointmentId);
    }
}
