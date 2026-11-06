package com.materialcrm.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.materialcrm.security.LoginAttemptLimiter;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final LoginAttemptLimiter loginAttemptLimiter;
    private final long tokenTtlMinutes;

    public AuthController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
            LoginAttemptLimiter loginAttemptLimiter, @Value("${APP_JWT_TTL_MINUTES:480}") long tokenTtlMinutes) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.loginAttemptLimiter = loginAttemptLimiter;
        this.tokenTtlMinutes = tokenTtlMinutes;
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        if (request.email() == null || request.password() == null
                || request.email().isBlank() || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe e-mail e senha.");
        }

        String email = request.email().trim();
        String remoteAddress = servletRequest.getRemoteAddr();
        loginAttemptLimiter.check(email, remoteAddress);
        List<UserRecord> users = jdbcTemplate.query(
                "SELECT id, email, password_hash, active, role FROM app_user WHERE lower(email) = lower(?)",
                (rs, rowNum) -> new UserRecord(
                        rs.getObject("id", UUID.class).toString(),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getBoolean("active"),
                        rs.getString("role")),
                email);

        if (users.isEmpty() || !users.get(0).active()
                || !passwordEncoder.matches(request.password(), users.get(0).passwordHash())) {
            loginAttemptLimiter.registerFailure(email, remoteAddress);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }

        UserRecord user = users.get(0);
        loginAttemptLimiter.registerSuccess(email, remoteAddress);
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(tokenTtlMinutes * 60);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("materialcrm-local")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.id())
                .claim("email", user.email())
                .claim("role", user.role())
                .build();

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
        return Map.of(
                "accessToken", token,
                "tokenType", "Bearer",
                "expiresAt", expiresAt.toString(),
                "user", Map.of("userId", user.id(), "email", user.email(), "role", user.role()));
    }

    @GetMapping("/me")
    public Map<String, Object> currentUser(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "userId", jwt.getSubject(),
                "email", jwt.getClaimAsString("email"),
                "role", jwt.getClaimAsString("role"));
    }

    public record LoginRequest(String email, String password) {}
    private record UserRecord(String id, String email, String passwordHash, boolean active, String role) {}
}
