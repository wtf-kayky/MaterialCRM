package com.materialcrm.api;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final JdbcTemplate jdbcTemplate;

    public DashboardController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @GetMapping("/summary")
    public DashboardSummary summary(@AuthenticationPrincipal Jwt jwt) {
        Scope scope = scope(jwt);
        BigDecimal salesTotal = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total), 0) FROM sale WHERE company_id = ? AND branch_id = ? AND status = 'COMPLETED' AND created_at::date = CURRENT_DATE",
                BigDecimal.class, scope.companyId(), scope.branchId());
        Integer salesCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM sale WHERE company_id = ? AND branch_id = ? AND status = 'COMPLETED' AND created_at::date = CURRENT_DATE",
                Integer.class, scope.companyId(), scope.branchId());
        Integer customerCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM customer WHERE company_id = ? AND active = true", Integer.class, scope.companyId());
        Integer productCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM product WHERE company_id = ? AND active = true", Integer.class, scope.companyId());
        Integer lowStockCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM stock WHERE company_id = ? AND branch_id = ? AND quantity <= minimum_quantity",
                Integer.class, scope.companyId(), scope.branchId());
        return new DashboardSummary(salesTotal, salesCount, customerCount, productCount, lowStockCount);
    }

    private Scope scope(Jwt jwt) {
        return jdbcTemplate.queryForObject("SELECT company_id, active_branch_id FROM app_user WHERE id = ? AND active = true",
                (rs, rowNum) -> new Scope(rs.getObject("company_id", UUID.class), rs.getObject("active_branch_id", UUID.class)),
                UUID.fromString(jwt.getSubject()));
    }

    public record DashboardSummary(BigDecimal salesTotal, int salesCount, int customerCount, int productCount, int lowStockCount) {}
    private record Scope(UUID companyId, UUID branchId) {}
}
