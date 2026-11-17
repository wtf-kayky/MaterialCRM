package com.materialcrm.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/opportunities")
public class OpportunityController {
    private final JdbcTemplate jdbcTemplate;
    public OpportunityController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @GetMapping
    public List<OpportunityView> list(@AuthenticationPrincipal Jwt jwt) {
        Scope scope = scope(jwt);
        return jdbcTemplate.query("""
                SELECT id, customer_id, title, stage, expected_amount, owner_user_id, closed_at, created_at, updated_at
                FROM crm_opportunity WHERE company_id = ? AND branch_id = ? ORDER BY updated_at DESC LIMIT 100
                """, (rs, rowNum) -> view(rs), scope.companyId(), scope.branchId());
    }

    @PostMapping
    @Transactional
    public OpportunityView create(@RequestBody CreateOpportunityRequest request, @AuthenticationPrincipal Jwt jwt) {
        Scope scope = requireSalesAccess(jwt);
        if (request.title() == null || request.title().isBlank() || request.title().trim().length() > 180
                || (request.expectedAmount() != null && request.expectedAmount().signum() < 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da oportunidade inválidos.");
        }
        if (request.customerId() != null && !customerExists(request.customerId(), scope)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente inválido para a filial ativa.");
        }
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO crm_opportunity (id, company_id, branch_id, customer_id, owner_user_id, title, expected_amount)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, scope.companyId(), scope.branchId(), request.customerId(), scope.userId(),
                request.title().trim(), request.expectedAmount());
        audit(scope, "OPPORTUNITY_CREATED", "CRM_OPPORTUNITY", id);
        return find(id, scope);
    }

    @PostMapping("/{opportunityId}/stage")
    @Transactional
    public OpportunityView moveStage(@PathVariable UUID opportunityId, @RequestBody MoveStageRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Scope scope = requireSalesAccess(jwt);
        if (request.stage() == null || !List.of("QUALIFIED", "PROPOSAL", "WON", "LOST").contains(request.stage())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Etapa inválida.");
        }
        List<String> stages = jdbcTemplate.query("""
                SELECT stage FROM crm_opportunity WHERE id = ? AND company_id = ? AND branch_id = ? FOR UPDATE
                """, (rs, rowNum) -> rs.getString("stage"), opportunityId, scope.companyId(), scope.branchId());
        if (stages.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada.");
        if ("WON".equals(stages.get(0)) || "LOST".equals(stages.get(0))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Oportunidade encerrada não pode ser reaberta nesta etapa.");
        }
        boolean closed = "WON".equals(request.stage()) || "LOST".equals(request.stage());
        jdbcTemplate.update("""
                UPDATE crm_opportunity SET stage = ?, closed_at = CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE NULL END,
                    updated_at = CURRENT_TIMESTAMP WHERE id = ? AND company_id = ? AND branch_id = ?
                """, request.stage(), closed, opportunityId, scope.companyId(), scope.branchId());
        audit(scope, "OPPORTUNITY_STAGE_CHANGED", "CRM_OPPORTUNITY", opportunityId);
        return find(opportunityId, scope);
    }

    private boolean customerExists(UUID customerId, Scope scope) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM customer WHERE id = ? AND company_id = ? AND branch_id = ? AND active = true", Integer.class, customerId, scope.companyId(), scope.branchId());
        return count != null && count > 0;
    }
    private OpportunityView find(UUID id, Scope scope) {
        List<OpportunityView> values = jdbcTemplate.query("SELECT id, customer_id, title, stage, expected_amount, owner_user_id, closed_at, created_at, updated_at FROM crm_opportunity WHERE id = ? AND company_id = ? AND branch_id = ?", (rs, rowNum) -> view(rs), id, scope.companyId(), scope.branchId());
        if (values.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada.");
        return values.get(0);
    }
    private OpportunityView view(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new OpportunityView(rs.getObject("id", UUID.class), rs.getObject("customer_id", UUID.class), rs.getString("title"), rs.getString("stage"), rs.getBigDecimal("expected_amount"), rs.getObject("owner_user_id", UUID.class), rs.getObject("closed_at", OffsetDateTime.class), rs.getObject("created_at", OffsetDateTime.class), rs.getObject("updated_at", OffsetDateTime.class));
    }
    private Scope requireSalesAccess(Jwt jwt) {
        Scope scope = scope(jwt);
        if (!List.of("ADMIN", "MANAGER", "SELLER").contains(scope.role())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão comercial.");
        return scope;
    }
    private Scope scope(Jwt jwt) {
        List<Scope> scopes = jdbcTemplate.query("""
                SELECT u.id, u.company_id, u.active_branch_id, u.role FROM app_user u
                JOIN user_branch_access uba ON uba.user_id = u.id AND uba.branch_id = u.active_branch_id
                JOIN branch b ON b.id = u.active_branch_id AND b.company_id = u.company_id AND b.active = true
                WHERE u.id = ? AND u.active = true
                """, (rs, rowNum) -> new Scope(rs.getObject("id", UUID.class), rs.getObject("company_id", UUID.class), rs.getObject("active_branch_id", UUID.class), rs.getString("role")), UUID.fromString(jwt.getSubject()));
        if (scopes.isEmpty()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário sem filial ativa autorizada.");
        return scopes.get(0);
    }
    private void audit(Scope scope, String event, String resourceType, UUID resourceId) {
        jdbcTemplate.update("INSERT INTO audit_event (id, company_id, actor_user_id, event_type, resource_type, resource_id) VALUES (?, ?, ?, ?, ?, ?)", UUID.randomUUID(), scope.companyId(), scope.userId(), event, resourceType, resourceId);
    }
    public record CreateOpportunityRequest(UUID customerId, String title, BigDecimal expectedAmount) {}
    public record MoveStageRequest(String stage) {}
    public record OpportunityView(UUID id, UUID customerId, String title, String stage, BigDecimal expectedAmount, UUID ownerUserId, OffsetDateTime closedAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
    private record Scope(UUID userId, UUID companyId, UUID branchId, String role) {}
}
