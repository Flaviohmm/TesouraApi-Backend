package com.tesoura.api.auth;

import com.tesoura.api.shared.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(properties.expirationMs());
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("salonId", user.getSalonId().toString())
                .claim("role", user.getRole().getValue())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public AuthPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new AuthPrincipal(
                UUID.fromString(claims.getSubject()),
                UUID.fromString(claims.get("salonId", String.class)),
                UserRole.fromValue(claims.get("role", String.class)),
                claims.get("email", String.class)
        );
    }

    public long expirationMs() {
        return properties.expirationMs();
    }
}
