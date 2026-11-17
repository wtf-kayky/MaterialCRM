package com.materialcrm.api;

import java.math.BigDecimal;
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
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final JdbcTemplate jdbcTemplate;

    public InventoryController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @GetMapping
    public List<StockView> list(@AuthenticationPrincipal Jwt jwt) {
        Scope scope = scope(jwt);
        return jdbcTemplate.query(
                "SELECT s.id, s.product_id, p.sku, p.name, s.quantity, s.minimum_quantity "
                        + "FROM stock s JOIN product p ON p.id = s.product_id "
                        + "WHERE s.company_id = ? AND s.branch_id = ? ORDER BY p.name",
                (rs, rowNum) -> new StockView(rs.getObject("id", UUID.class), rs.getObject("product_id", UUID.class),
                        rs.getString("sku"), rs.getString("name"), rs.getBigDecimal("quantity"),
                        rs.getBigDecimal("minimum_quantity")), scope.companyId(), scope.branchId());
    }

    @PostMapping("/movements")
    @Transactional
    public StockView movement(@RequestBody MovementRequest request, @AuthenticationPrincipal Jwt jwt) {
        Scope scope = requireInventoryAccess(jwt);
        if (request.productId() == null || request.quantity() == null || request.quantity().signum() <= 0
                || request.movementType() == null || (!request.movementType().equals("IN") && !request.movementType().equals("OUT")
                        && !request.movementType().equals("ADJUST"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados da movimentação inválidos.");
        }
        Integer productCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM product WHERE id = ? AND company_id = ? AND active = true",
                Integer.class, request.productId(), scope.companyId());
        if (productCount == null || productCount == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado.");
        List<BigDecimal> currentValues = jdbcTemplate.query(
                "SELECT quantity FROM stock WHERE branch_id = ? AND product_id = ? FOR UPDATE",
                (rs, rowNum) -> rs.getBigDecimal("quantity"), scope.branchId(), request.productId());
        BigDecimal current = currentValues.isEmpty() ? BigDecimal.ZERO : currentValues.get(0);
        BigDecimal next = request.movementType().equals("IN") ? current.add(request.quantity())
                : request.movementType().equals("OUT") ? current.subtract(request.quantity()) : request.quantity();
        if (next.signum() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque insuficiente.");
        jdbcTemplate.update("INSERT INTO stock (company_id, branch_id, product_id, quantity) VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (branch_id, product_id) DO UPDATE SET quantity = EXCLUDED.quantity, updated_at = CURRENT_TIMESTAMP",
                scope.companyId(), scope.branchId(), request.productId(), next);
        jdbcTemplate.update(
                "INSERT INTO stock_movement (company_id, branch_id, product_id, movement_type, quantity, note) VALUES (?, ?, ?, ?, ?, ?)",
                scope.companyId(), scope.branchId(), request.productId(), request.movementType(), request.quantity(), request.note());
        return find(request.productId(), scope);
    }

    private StockView find(UUID productId, Scope scope) {
        return jdbcTemplate.queryForObject(
                "SELECT s.id, s.product_id, p.sku, p.name, s.quantity, s.minimum_quantity FROM stock s "
                        + "JOIN product p ON p.id = s.product_id WHERE s.branch_id = ? AND s.product_id = ?",
                (rs, rowNum) -> new StockView(rs.getObject("id", UUID.class), rs.getObject("product_id", UUID.class),
                        rs.getString("sku"), rs.getString("name"), rs.getBigDecimal("quantity"),
                        rs.getBigDecimal("minimum_quantity")), scope.branchId(), productId);
    }

    private Scope requireInventoryAccess(Jwt jwt) {
        Scope scope = scope(jwt);
        if (!"ADMIN".equals(scope.role()) && !"MANAGER".equals(scope.role()) && !"STOCK".equals(scope.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão para movimentar estoque.");
        }
        return scope;
    }

    private Scope scope(Jwt jwt) {
        return jdbcTemplate.queryForObject(
                "SELECT u.company_id, u.active_branch_id, u.role FROM app_user u "
                        + "JOIN user_branch_access uba ON uba.user_id = u.id AND uba.branch_id = u.active_branch_id "
                        + "JOIN branch b ON b.id = u.active_branch_id AND b.company_id = u.company_id AND b.active = true "
                        + "WHERE u.id = ? AND u.active = true",
                (rs, rowNum) -> new Scope(rs.getObject("company_id", UUID.class),
                        rs.getObject("active_branch_id", UUID.class), rs.getString("role")),
                UUID.fromString(jwt.getSubject()));
    }

    public record MovementRequest(UUID productId, String movementType, BigDecimal quantity, String note) {}
    public record StockView(UUID id, UUID productId, String sku, String name, BigDecimal quantity, BigDecimal minimumQuantity) {}
    private record Scope(UUID companyId, UUID branchId, String role) {}
}