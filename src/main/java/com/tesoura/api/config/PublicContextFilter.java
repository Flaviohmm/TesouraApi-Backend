package com.tesoura.api.config;

import com.tesoura.api.auth.JwtService;
import com.tesoura.api.customer.CustomerContext;
import com.tesoura.api.customer.CustomerPrincipal;
import com.tesoura.api.salon.Salon;
import com.tesoura.api.salon.SalonRepository;
import com.tesoura.api.shared.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mirrors {@link TenantContextFilter} for the public/customer-facing surface under
 * {@code /api/public/**}. Resolves the salon from the {slug} path segment (so the existing
 * tenant-scoped services keep working unmodified) and, if a customer bearer token is present,
 * sets {@link CustomerContext} — optional, since browsing does not require login.
 */
@Component
public class PublicContextFilter extends OncePerRequestFilter {

    private static final Pattern SALON_SLUG_PATH = Pattern.compile("^/api/public/salons/([^/]+)(?:/.*)?$");

    private final SalonRepository salonRepository;
    private final JwtService jwtService;

    public PublicContextFilter(SalonRepository salonRepository, JwtService jwtService) {
        this.salonRepository = salonRepository;
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.startsWith("/api/public") && !path.startsWith("/api/customer-auth");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        try {
            Matcher matcher = SALON_SLUG_PATH.matcher(request.getServletPath());
            if (matcher.matches()) {
                Optional<Salon> salon = salonRepository.findBySlugIgnoreCase(matcher.group(1));
                if (salon.isEmpty()) {
                    writeError(response, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Salão não encontrado");
                    return;
                }
                TenantContext.set(salon.get().getId());
            }

            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7).trim();
                if (!token.isEmpty()) {
                    try {
                        CustomerPrincipal principal = jwtService.parseCustomer(token);
                        CustomerContext.set(principal);
                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                        principal, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                                )
                        );
                    } catch (Exception ex) {
                        writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Token inválido ou expirado");
                        return;
                    }
                }
            }

            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            CustomerContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }
}
