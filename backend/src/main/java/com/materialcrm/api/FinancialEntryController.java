package com.materialcrm.api;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@RequestMapping("/api/v1/financial-entries")
public class FinancialEntryController {
    private final JdbcTemplate jdbcTemplate;

    public FinancialEntryController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<FinancialEntryView> list(@AuthenticationPrincipal Jwt jwt) {
        Scope scope = scope(jwt);
        return jdbcTemplate.query("""
                SELECT id, entry_type, status, description, counterparty, amount, due_date, settled_at, created_at
                FROM financial_entry
                WHERE company_id = ? AND branch_id = ?
                ORDER BY created_at DESC
                LIMIT 100
                """, (rs, rowNum) -> view(rs), scope.companyId(), scope.branchId());
    }

    @PostMapping
    @Transactional
    public FinancialEntryView create(@RequestBody CreateFinancialEntryRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Scope scope = requireManagementAccess(jwt);
        validateCreateRequest(request);
        UUID entryId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO financial_entry (id, company_id, branch_id, entry_type, description, counterparty, amount, due_date, created_by_user_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, entryId, scope.companyId(), scope.branchId(), request.entryType(), request.description().trim(),
                blankToNull(request.counterparty()), request.amount(), request.dueDate(), scope.userId());
        recordAudit(scope, "FINANCIAL_ENTRY_CREATED", entryId);
        return find(entryId, scope);
    }

    @PostMapping("/{entryId}/settle")
    @Transactional
    public FinancialEntryView settle(@PathVariable UUID entryId, @AuthenticationPrincipal Jwt jwt) {
        Scope scope = requireSettlementAccess(jwt);
        List<String> statuses = jdbcTemplate.query("""
                SELECT status
                FROM financial_entry
                WHERE id = ? AND company_id = ? AND branch_id = ?
                FOR UPDATE
                """, (rs, rowNum) -> rs.getString("status"), entryId, scope.companyId(), scope.branchId());
        if (statuses.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lançamento financeiro não encontrado.");
        }
        if (!"OPEN".equals(statuses.get(0))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Apenas lançamentos em aberto podem ser baixados.");
        }
        jdbcTemplate.update("""
                UPDATE financial_entry
                SET status = 'SETTLED', settled_at = CURRENT_TIMESTAMP, settled_by_user_id = ?
                WHERE id = ? AND company_id = ? AND branch_id = ? AND status = 'OPEN'
                """, scope.userId(), entryId, scope.companyId(), scope.branchId());
        recordAudit(scope, "FINANCIAL_ENTRY_SETTLED", entryId);
        return find(entryId, scope);
    }

    private FinancialEntryView find(UUID entryId, Scope scope) {
        List<FinancialEntryView> entries = jdbcTemplate.query("""
                SELECT id, entry_type, status, description, counterparty, amount, due_date, settled_at, created_at
                FROM financial_entry
                WHERE id = ? AND company_id = ? AND branch_id = ?
                """, (rs, rowNum) -> view(rs), entryId, scope.companyId(), scope.branchId());
        if (entries.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lançamento financeiro não encontrado.");
        }
        return entries.get(0);
    }

    private FinancialEntryView view(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new FinancialEntryView(rs.getObject("id", UUID.class), rs.getString("entry_type"),
                rs.getString("status"), rs.getString("description"), rs.getString("counterparty"),
                rs.getBigDecimal("amount"), rs.getObject("due_date", LocalDate.class),
                rs.getObject("settled_at", OffsetDateTime.class), rs.getObject("created_at", OffsetDateTime.class));
    }

    private void validateCreateRequest(CreateFinancialEntryRequest request) {
        if (request.entryType() == null || (!"RECEIVABLE".equals(request.entryType())
                && !"PAYABLE".equals(request.entryType())) || request.amount() == null
                || request.amount().signum() <= 0 || request.description() == null || request.description().isBlank()
                || request.description().trim().length() > 250
                || (request.counterparty() != null && request.counterparty().trim().length() > 150)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados do lançamento financeiro inválidos.");
        }
    }

    private Scope requireManagementAccess(Jwt jwt) {
        Scope scope = scope(jwt);
        if (!"ADMIN".equals(scope.role()) && !"MANAGER".equals(scope.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão para criar lançamentos.");
        }
        return scope;
    }

    private Scope requireSettlementAccess(Jwt jwt) {
        Scope scope = scope(jwt);
        if (!"ADMIN".equals(scope.role()) && !"MANAGER".equals(scope.role()) && !"CASHIER".equals(scope.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão para baixar lançamentos.");
        }
        return scope;
    }

    private Scope scope(Jwt jwt) {
        List<Scope> scopes = jdbcTemplate.query("""
                SELECT u.id, u.company_id, u.active_branch_id, u.role
                FROM app_user u
                JOIN user_branch_access uba ON uba.user_id = u.id AND uba.branch_id = u.active_branch_id
                JOIN branch b ON b.id = u.active_branch_id AND b.company_id = u.company_id AND b.active = true
                WHERE u.id = ? AND u.active = true
                """, (rs, rowNum) -> new Scope(rs.getObject("id", UUID.class),
                rs.getObject("company_id", UUID.class), rs.getObject("active_branch_id", UUID.class),
                rs.getString("role")), UUID.fromString(jwt.getSubject()));
        if (scopes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário sem filial ativa autorizada.");
        }
        return scopes.get(0);
    }

    private void recordAudit(Scope scope, String eventType, UUID entryId) {
        jdbcTemplate.update("""
                INSERT INTO audit_event (id, company_id, actor_user_id, event_type, resource_type, resource_id)
                VALUES (?, ?, ?, ?, 'FINANCIAL_ENTRY', ?)
                """, UUID.randomUUID(), scope.companyId(), scope.userId(), eventType, entryId);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record CreateFinancialEntryRequest(String entryType, String description, String counterparty,
            BigDecimal amount, LocalDate dueDate) {}

    public record FinancialEntryView(UUID id, String entryType, String status, String description,
            String counterparty, BigDecimal amount, LocalDate dueDate, OffsetDateTime settledAt,
            OffsetDateTime createdAt) {}

    private record Scope(UUID userId, UUID companyId, UUID branchId, String role) {}
}
