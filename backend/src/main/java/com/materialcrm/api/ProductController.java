package com.materialcrm.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final JdbcTemplate jdbcTemplate;

    public ProductController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<ProductView> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String search) {
        UUID companyId = companyId(jwt);
        String term = search == null ? "" : search.trim();
        return jdbcTemplate.query(
                "SELECT id, sku, name, category, unit, cost_price, sale_price, active FROM product "
                        + "WHERE company_id = ? AND (lower(name) LIKE lower(?) OR lower(sku) LIKE lower(?)) ORDER BY name LIMIT 100",
                (rs, rowNum) -> new ProductView(rs.getObject("id", UUID.class), rs.getString("sku"),
                        rs.getString("name"), rs.getString("category"), rs.getString("unit"),
                        rs.getBigDecimal("cost_price"), rs.getBigDecimal("sale_price"), rs.getBoolean("active")),
                companyId, "%" + term + "%", "%" + term + "%");
    }

    @PostMapping
    public ProductView create(@RequestBody ProductRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID companyId = requireManagement(jwt);
        validate(request);
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO product (id, company_id, sku, name, category, unit, cost_price, sale_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, companyId, request.sku().trim(), request.name().trim(), blankToNull(request.category()),
                request.unit().trim().toUpperCase(), request.costPrice(), request.salePrice());
        return find(id, companyId);
    }

    @PutMapping("/{productId}")
    public ProductView update(@PathVariable UUID productId, @RequestBody ProductRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID companyId = requireManagement(jwt);
        validate(request);
        int updated = jdbcTemplate.update(
                "UPDATE product SET sku = ?, name = ?, category = ?, unit = ?, cost_price = ?, sale_price = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND company_id = ?",
                request.sku().trim(), request.name().trim(), blankToNull(request.category()), request.unit().trim().toUpperCase(),
                request.costPrice(), request.salePrice(), productId, companyId);
        if (updated == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado.");
        return find(productId, companyId);
    }

    @DeleteMapping("/{productId}")
    public void deactivate(@PathVariable UUID productId, @AuthenticationPrincipal Jwt jwt) {
        UUID companyId = requireManagement(jwt);
        int updated = jdbcTemplate.update("UPDATE product SET active = false, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND company_id = ?",
                productId, companyId);
        if (updated == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado.");
    }

    private ProductView find(UUID id, UUID companyId) {
        return jdbcTemplate.queryForObject(
                "SELECT id, sku, name, category, unit, cost_price, sale_price, active FROM product WHERE id = ? AND company_id = ?",
                (rs, rowNum) -> new ProductView(rs.getObject("id", UUID.class), rs.getString("sku"), rs.getString("name"),
                        rs.getString("category"), rs.getString("unit"), rs.getBigDecimal("cost_price"),
                        rs.getBigDecimal("sale_price"), rs.getBoolean("active")), id, companyId);
    }

    private UUID requireManagement(Jwt jwt) {
        String role = jdbcTemplate.queryForObject("SELECT role FROM app_user WHERE id = ? AND active = true", String.class,
                UUID.fromString(jwt.getSubject()));
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão para produtos.");
        }
        return companyId(jwt);
    }

    private UUID companyId(Jwt jwt) {
        return jdbcTemplate.queryForObject("SELECT company_id FROM app_user WHERE id = ? AND active = true", UUID.class,
                UUID.fromString(jwt.getSubject()));
    }

    private void validate(ProductRequest request) {
        if (request.sku() == null || request.sku().isBlank() || request.name() == null || request.name().isBlank()
                || request.unit() == null || request.unit().isBlank() || request.costPrice() == null
                || request.salePrice() == null || request.costPrice().signum() < 0 || request.salePrice().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados do produto inválidos.");
        }
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record ProductRequest(String sku, String name, String category, String unit,
            BigDecimal costPrice, BigDecimal salePrice) {}
    public record ProductView(UUID id, String sku, String name, String category, String unit,
            BigDecimal costPrice, BigDecimal salePrice, boolean active) {}
}
