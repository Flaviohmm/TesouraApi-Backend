package com.tesoura.api.customer;

import com.tesoura.api.auth.JwtService;
import com.tesoura.api.customer.dto.CustomerLoginRequest;
import com.tesoura.api.customer.dto.CustomerLoginResponse;
import com.tesoura.api.customer.dto.CustomerMeResponse;
import com.tesoura.api.customer.dto.CustomerRegisterRequest;
import com.tesoura.api.shared.exception.ApiException;
import com.tesoura.api.shared.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerAuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public CustomerAuthService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public CustomerLoginResponse register(CustomerRegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("E-mail já cadastrado");
        }

        Customer customer = new Customer();
        customer.setName(request.name().trim());
        customer.setEmail(email);
        customer.setPhone(request.phone());
        customer.setPasswordHash(passwordEncoder.encode(request.password()));
        customer = customerRepository.save(customer);

        return toLoginResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerLoginResponse login(CustomerLoginRequest request) {
        Customer customer = customerRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> ApiException.unauthorized("Credenciais inválidas"));
        if (!passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            throw ApiException.unauthorized("Credenciais inválidas");
        }
        return toLoginResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerMeResponse me() {
        CustomerPrincipal principal = CustomerContext.require();
        Customer customer = customerRepository.findById(principal.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        return new CustomerMeResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone()
        );
    }

    private CustomerLoginResponse toLoginResponse(Customer customer) {
        return new CustomerLoginResponse(
                jwtService.generateCustomerToken(customer),
                "Bearer",
                jwtService.expirationMs() / 1000,
                customer.getId(),
                customer.getName()
        );
    }
}
