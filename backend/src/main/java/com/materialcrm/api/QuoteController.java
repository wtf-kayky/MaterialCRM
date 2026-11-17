package com.materialcrm.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {
    private final JdbcTemplate jdbcTemplate;
    public QuoteController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @GetMapping
    public List<QuoteView> list(@AuthenticationPrincipal Jwt jwt) {
        Scope scope = scope(jwt);
        return jdbcTemplate.query("SELECT id, customer_id, opportunity_id, status, total, valid_until, created_at FROM quote WHERE company_id = ? AND branch_id = ? ORDER BY created_at DESC LIMIT 100", (rs, rowNum) -> view(rs), scope.companyId(), scope.branchId());
    }

    @PostMapping
    @Transactional
    public QuoteView create(@RequestBody CreateQuoteRequest request, @AuthenticationPrincipal Jwt jwt) {
        Scope scope = requireSalesAccess(jwt);
        if (request.customerId() == null || request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe cliente e itens do orçamento.");
        }
        if (!customerExists(request.customerId(), scope) || (request.opportunityId() != null && !opportunityExists(request.opportunityId(), scope))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente ou oportunidade inválidos para a filial ativa.");
        }
        UUID quoteId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO quote (id, company_id, branch_id, customer_id, opportunity_id, created_by_user_id, valid_until) VALUES (?, ?, ?, ?, ?, ?, ?)", quoteId, scope.companyId(), scope.branchId(), request.customerId(), request.opportunityId(), scope.userId(), request.validUntil());
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (QuoteItemRequest item : request.items()) {
            if (item.productId() == null || item.quantity() == null || item.quantity().signum() <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item do orçamento inválido.");
            List<BigDecimal> prices = jdbcTemplate.query("SELECT sale_price FROM product WHERE id = ? AND company_id = ? AND active = true", (rs, rowNum) -> rs.getBigDecimal("sale_price"), item.productId(), scope.companyId());
            if (prices.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado.");
            BigDecimal unitPrice = prices.get(0);
            BigDecimal itemTotal = unitPrice.multiply(item.quantity()).setScale(2, RoundingMode.HALF_UP);
            jdbcTemplate.update("INSERT INTO quote_item (quote_id, product_id, quantity, unit_price, total) VALUES (?, ?, ?, ?, ?)", quoteId, item.productId(), item.quantity(), unitPrice, itemTotal);
            total = total.add(itemTotal);
        }
        jdbcTemplate.update("UPDATE quote SET total = ? WHERE id = ?", total, quoteId);
        audit(scope, "QUOTE_CREATED", quoteId);
        return find(quoteId, scope);
    }

    private boolean customerExists(UUID id, Scope scope) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM customer WHERE id = ? AND company_id = ? AND branch_id = ? AND active = true", Integer.class, id, scope.companyId(), scope.branchId());
        return count != null && count > 0;
    }
    private boolean opportunityExists(UUID id, Scope scope) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM crm_opportunity WHERE id = ? AND company_id = ? AND branch_id = ?", Integer.class, id, scope.companyId(), scope.branchId());
        return count != null && count > 0;
    }
    private QuoteView find(UUID id, Scope scope) {
        List<QuoteView> values = jdbcTemplate.query("SELECT id, customer_id, opportunity_id, status, total, valid_until, created_at FROM quote WHERE id = ? AND company_id = ? AND branch_id = ?", (rs, rowNum) -> view(rs), id, scope.companyId(), scope.branchId());
        if (values.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado.");
        return values.get(0);
    }
    private QuoteView view(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new QuoteView(rs.getObject("id", UUID.class), rs.getObject("customer_id", UUID.class), rs.getObject("opportunity_id", UUID.class), rs.getString("status"), rs.getBigDecimal("total"), rs.getObject("valid_until", LocalDate.class), rs.getObject("created_at", OffsetDateTime.class));
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
    private void audit(Scope scope, String event, UUID quoteId) {
        jdbcTemplate.update("INSERT INTO audit_event (id, company_id, actor_user_id, event_type, resource_type, resource_id) VALUES (?, ?, ?, ?, 'QUOTE', ?)", UUID.randomUUID(), scope.companyId(), scope.userId(), event, quoteId);
    }
    public record CreateQuoteRequest(UUID customerId, UUID opportunityId, LocalDate validUntil, List<QuoteItemRequest> items) {}
    public record QuoteItemRequest(UUID productId, BigDecimal quantity) {}
    public record QuoteView(UUID id, UUID customerId, UUID opportunityId, String status, BigDecimal total, LocalDate validUntil, OffsetDateTime createdAt) {}
    private record Scope(UUID userId, UUID companyId, UUID branchId, String role) {}
}
