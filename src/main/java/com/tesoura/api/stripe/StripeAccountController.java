package com.tesoura.api.stripe;

import com.tesoura.api.shared.tenant.TenantContext;
import com.tesoura.api.stripe.dto.StripeAccountStatusDTO;
import com.tesoura.api.stripe.dto.StripeAuthorizationUrlDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/salons/me/stripe")
public class StripeAccountController {

    private final StripeAccountService accountService;

    public StripeAccountController(StripeAccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/connect")
    public StripeAuthorizationUrlDTO connect() {
        return accountService.createOnboardingLink(TenantContext.get());
    }

    @GetMapping("/status")
    public StripeAccountStatusDTO status() {
        return accountService.getStatus(TenantContext.get());
    }
}
