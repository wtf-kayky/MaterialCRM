package com.materialcrm.audit;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-events")
public class AuditEventController {
    private final JdbcTemplate jdbcTemplate;

    public AuditEventController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    public List<AuditEventResponse> list(@RequestParam(defaultValue = "50") int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 100));
        return jdbcTemplate.query(
                "select id, event_type, resource_type, resource_id, actor_user_id, occurred_at "
                        + "from audit_event order by occurred_at desc limit ?",
                (rs, rowNum) -> new AuditEventResponse(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("event_type"),
                        rs.getString("resource_type"),
                        rs.getString("resource_id") == null ? null : UUID.fromString(rs.getString("resource_id")),
                        rs.getString("actor_user_id") == null ? null : UUID.fromString(rs.getString("actor_user_id")),
                        rs.getObject("occurred_at", OffsetDateTime.class)),
                boundedLimit);
    }

    public record AuditEventResponse(UUID id, String eventType, String resourceType, UUID resourceId,
                                     UUID actorUserId, OffsetDateTime occurredAt) { }
}