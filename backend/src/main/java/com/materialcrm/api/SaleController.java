package com.materialcrm.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
@RequestMapping("/api/v1/sales")
public class SaleController {
    private final JdbcTemplate jdbcTemplate;

    public SaleController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @GetMapping
    public List<SaleView> list(@AuthenticationPrincipal Jwt jwt) {
        Scope scope = scope(jwt);
        return jdbcTemplate.query(
                "SELECT id, customer_id, payment_method, status, total, created_at FROM sale WHERE company_id = ? AND branch_id = ? ORDER BY created_at DESC LIMIT 100",
                (rs, rowNum) -> new SaleView(rs.getObject("id", UUID.class), rs.getObject("customer_id", UUID.class),
                        rs.getString("payment_method"), rs.getString("status"), rs.getBigDecimal("total"),
                        rs.getObject("created_at", java.time.OffsetDateTime.class)), scope.companyId(), scope.branchId());
    }

    @PostMapping
    @Transactional
    public SaleView create(@RequestBody SaleRequest request, @AuthenticationPrincipal Jwt jwt) {
        Scope scope = scope(jwt);
        if (request.items() == null || request.items().isEmpty() || request.paymentMethod() == null || request.paymentMethod().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe itens e forma de pagamento.");
        }
        if (request.customerId() != null) {
            Integer customerCount = jdbcTemplate.queryForObject("SELECT count(*) FROM customer WHERE id = ? AND company_id = ? AND active = true",
                    Integer.class, request.customerId(), scope.companyId());
            if (customerCount == null || customerCount == 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente inválido.");
        }
        UUID saleId = UUID.randomUUID();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        jdbcTemplate.update("INSERT INTO sale (id, company_id, branch_id, customer_id, seller_user_id, payment_method, total) VALUES (?, ?, ?, ?, ?, ?, 0)",
                saleId, scope.companyId(), scope.branchId(), request.customerId(), scope.userId(), request.paymentMethod().trim().toUpperCase());
        for (SaleItemRequest item : request.items()) {
            if (item.productId() == null || item.quantity() == null || item.quantity().signum() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item de venda inválido.");
            }
            ProductData product = jdbcTemplate.queryForObject("SELECT p.sale_price, s.quantity FROM product p LEFT JOIN stock s ON s.product_id = p.id AND s.branch_id = ? WHERE p.id = ? AND p.company_id = ? AND p.active = true",
                    (rs, rowNum) -> new ProductData(rs.getBigDecimal("sale_price"), rs.getBigDecimal("quantity")), scope.branchId(), item.productId(), scope.companyId());
            BigDecimal available = product.quantity() == null ? BigDecimal.ZERO : product.quantity();
            if (available.compareTo(item.quantity()) < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque insuficiente para o produto.");
            BigDecimal itemTotal = product.salePrice().multiply(item.quantity()).setScale(2, RoundingMode.HALF_UP);
            total = total.add(itemTotal);
            jdbcTemplate.update("INSERT INTO sale_item (sale_id, product_id, quantity, unit_price, total) VALUES (?, ?, ?, ?, ?)", saleId, item.productId(), item.quantity(), product.salePrice(), itemTotal);
            jdbcTemplate.update("UPDATE stock SET quantity = quantity - ?, updated_at = CURRENT_TIMESTAMP WHERE branch_id = ? AND product_id = ?", item.quantity(), scope.branchId(), item.productId());
            jdbcTemplate.update("INSERT INTO stock_movement (company_id, branch_id, product_id, movement_type, quantity, note) VALUES (?, ?, ?, 'OUT', ?, ?)", scope.companyId(), scope.branchId(), item.productId(), item.quantity(), "Venda " + saleId);
        }
        jdbcTemplate.update("UPDATE sale SET total = ? WHERE id = ?", total, saleId);
        return find(saleId, scope);
    }

    private SaleView find(UUID id, Scope scope) {
        return jdbcTemplate.queryForObject("SELECT id, customer_id, payment_method, status, total, created_at FROM sale WHERE id = ? AND company_id = ?",
                (rs, rowNum) -> new SaleView(rs.getObject("id", UUID.class), rs.getObject("customer_id", UUID.class), rs.getString("payment_method"), rs.getString("status"), rs.getBigDecimal("total"), rs.getObject("created_at", java.time.OffsetDateTime.class)), id, scope.companyId());
    }

    private Scope scope(Jwt jwt) {
        return jdbcTemplate.queryForObject("SELECT id, company_id, active_branch_id FROM app_user WHERE id = ? AND active = true",
                (rs, rowNum) -> new Scope(rs.getObject("id", UUID.class), rs.getObject("company_id", UUID.class), rs.getObject("active_branch_id", UUID.class)), UUID.fromString(jwt.getSubject()));
    }

    public record SaleRequest(UUID customerId, String paymentMethod, List<SaleItemRequest> items) {}
    public record SaleItemRequest(UUID productId, BigDecimal quantity) {}
    public record SaleView(UUID id, UUID customerId, String paymentMethod, String status, BigDecimal total, java.time.OffsetDateTime createdAt) {}
    private record Scope(UUID userId, UUID companyId, UUID branchId) {}
    private record ProductData(BigDecimal salePrice, BigDecimal quantity) {}
}
