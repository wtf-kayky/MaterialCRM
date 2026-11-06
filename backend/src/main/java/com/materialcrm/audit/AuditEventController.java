package com.materialcrm.audit;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/audit-events")
public class AuditEventController {
    private final JdbcTemplate jdbcTemplate;

    public AuditEventController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<AuditEventResponse> list(@RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String role = jwt.getClaimAsString("role");
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão para auditoria.");
        }
        int boundedLimit = Math.max(1, Math.min(limit, 100));
        return jdbcTemplate.query(
                "SELECT id, event_type, resource_type, resource_id, actor_user_id, occurred_at "
                        + "FROM audit_event WHERE company_id = (SELECT company_id FROM app_user WHERE id = ? AND active = true) "
                        + "ORDER BY occurred_at DESC LIMIT ?",
                (rs, rowNum) -> new AuditEventResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("event_type"),
                        rs.getString("resource_type"),
                        rs.getObject("resource_id", UUID.class),
                        rs.getObject("actor_user_id", UUID.class),
                        rs.getObject("occurred_at", OffsetDateTime.class)),
                userId,
                boundedLimit);
    }

    public record AuditEventResponse(UUID id, String eventType, String resourceType, UUID resourceId,
                                     UUID actorUserId, OffsetDateTime occurredAt) { }
}
