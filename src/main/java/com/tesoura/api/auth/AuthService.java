package com.tesoura.api.auth;

import com.tesoura.api.auth.dto.LoginRequest;
import com.tesoura.api.auth.dto.LoginResponse;
import com.tesoura.api.auth.dto.MeResponse;
import com.tesoura.api.auth.dto.RegisterRequest;
import com.tesoura.api.professional.Professional;
import com.tesoura.api.professional.ProfessionalRepository;
import com.tesoura.api.salon.Salon;
import com.tesoura.api.salon.SalonRepository;
import com.tesoura.api.shared.enums.SubscriptionPlan;
import com.tesoura.api.shared.enums.UserRole;
import com.tesoura.api.shared.exception.ApiException;
import com.tesoura.api.shared.exception.ResourceNotFoundException;
import com.tesoura.api.shared.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SalonRepository salonRepository;
    private final ProfessionalRepository professionalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final int trialDays;

    public AuthService(
            UserRepository userRepository,
            SalonRepository salonRepository,
            ProfessionalRepository professionalRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${app.billing.trial-days:14}") int trialDays
    ) {
        this.userRepository = userRepository;
        this.salonRepository = salonRepository;
        this.professionalRepository = professionalRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.trialDays = trialDays;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String slug = request.slug().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("E-mail já cadastrado");
        }
        if (salonRepository.existsBySlugIgnoreCase(slug)) {
            throw ApiException.conflict("Slug já está em uso");
        }

        Salon salon = new Salon();
        salon.setName(request.salonName().trim());
        salon.setSlug(slug);
        salon.setPhone(request.phone());
        salon.setSubscriptionPlan(SubscriptionPlan.TRIAL);
        salon.setSubscriptionStatus("active");
        salon.setTrialEndsAt(Instant.now().plus(trialDays, ChronoUnit.DAYS));
        salon = salonRepository.save(salon);

        User user = new User();
        user.setSalonId(salon.getId());
        user.setName(request.ownerName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setRole(UserRole.OWNER);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user = userRepository.save(user);

        Professional professional = new Professional();
        professional.setSalonId(salon.getId());
        professional.setUserId(user.getId());
        professional.setName(user.getName());
        professional.setActive(true);
        professionalRepository.save(professional);

        return toLoginResponse(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> ApiException.unauthorized("Credenciais inválidas"));
        if (!user.isActive() || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Credenciais inválidas");
        }
        return toLoginResponse(user);
    }

    @Transactional(readOnly = true)
    public MeResponse me() {
        AuthPrincipal principal = currentPrincipal();
        User user = userRepository.findByIdAndSalonId(principal.userId(), TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return new MeResponse(
                user.getId(),
                user.getSalonId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().getValue()
        );
    }

    private LoginResponse toLoginResponse(User user) {
        return new LoginResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.expirationMs() / 1000,
                user.getId(),
                user.getSalonId(),
                user.getName(),
                user.getRole().getValue()
        );
    }

    private AuthPrincipal currentPrincipal() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw ApiException.unauthorized("Não autenticado");
        }
        return principal;
    }
}
