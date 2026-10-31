package com.materialcrm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialAdminInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public InitialAdminInitializer(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder,
            @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String email,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.isBlank() || password.length() < 12) {
            throw new IllegalStateException("Informe e-mail e senha inicial com pelo menos 12 caracteres.");
        }
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM app_user", Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO app_user (email, password_hash, active, role) VALUES (lower(?), ?, true, 'ADMIN') ON CONFLICT DO NOTHING",
                    email.trim(), passwordEncoder.encode(password));
        }
    }
}
