package com.tesoura.api.customer;

import com.tesoura.api.customer.dto.CustomerLoginRequest;
import com.tesoura.api.customer.dto.CustomerLoginResponse;
import com.tesoura.api.customer.dto.CustomerMeResponse;
import com.tesoura.api.customer.dto.CustomerRegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer-auth")
public class CustomerAuthController {

    private final CustomerAuthService customerAuthService;

    public CustomerAuthController(CustomerAuthService customerAuthService) {
        this.customerAuthService = customerAuthService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerLoginResponse register(@Valid @RequestBody CustomerRegisterRequest request) {
        return customerAuthService.register(request);
    }

    @PostMapping("/login")
    public CustomerLoginResponse login(@Valid @RequestBody CustomerLoginRequest request) {
        return customerAuthService.login(request);
    }

    @GetMapping("/me")
    public CustomerMeResponse me() {
        return customerAuthService.me();
    }
}
