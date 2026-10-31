package com.materialcrm.audit;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationAuditListener {
    private final JdbcTemplate jdbcTemplate;

    public AuthenticationAuditListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        var users = jdbcTemplate.query(
                "select id, company_id from app_user where lower(email) = lower(?)",
                (rs, rowNum) -> new UserReference(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("company_id"))),
                event.getAuthentication().getName());

        if (users.isEmpty()) {
            return;
        }

        var user = users.getFirst();
        jdbcTemplate.update(
                "insert into audit_event (id, company_id, actor_user_id, event_type, resource_type, resource_id) "
                        + "values (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                user.companyId(),
                user.id(),
                "LOGIN_SUCCEEDED",
                "APP_USER",
                user.id());
    }

    private record UserReference(UUID id, UUID companyId) { }
}