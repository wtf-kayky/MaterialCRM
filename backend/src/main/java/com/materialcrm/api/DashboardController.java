package com.materialcrm.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final JdbcTemplate jdbcTemplate;

    public DashboardController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @GetMapping("/summary")
    public DashboardSummary summary(@RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to, @AuthenticationPrincipal Jwt jwt) {
        Scope scope = requireDashboardAccess(jwt);
        LocalDate start = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate end = to == null ? LocalDate.now() : to;
        if (end.isBefore(start) || end.isAfter(start.plusDays(366))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Período inválido.");
        }

        BigDecimal salesTotal = amount("SELECT COALESCE(SUM(total), 0) FROM sale WHERE company_id = ? AND branch_id = ? AND status = 'COMPLETED' AND created_at >= ? AND created_at < ?", scope, start, end.plusDays(1));
        long salesCount = count("SELECT count(*) FROM sale WHERE company_id = ? AND branch_id = ? AND status = 'COMPLETED' AND created_at >= ? AND created_at < ?", scope, start, end.plusDays(1));
        BigDecimal receivableOpen = amount("SELECT COALESCE(SUM(amount), 0) FROM financial_entry WHERE company_id = ? AND branch_id = ? AND entry_type = 'RECEIVABLE' AND status = 'OPEN'", scope);
        BigDecimal payableOpen = amount("SELECT COALESCE(SUM(amount), 0) FROM financial_entry WHERE company_id = ? AND branch_id = ? AND entry_type = 'PAYABLE' AND status = 'OPEN'", scope);
        long lowStockCount = count("SELECT count(*) FROM stock WHERE company_id = ? AND branch_id = ? AND quantity <= minimum_quantity", scope);
        Map<String, Long> opportunities = Map.of(
                "new", count("SELECT count(*) FROM crm_opportunity WHERE company_id = ? AND branch_id = ? AND stage = 'NEW'", scope),
                "qualified", count("SELECT count(*) FROM crm_opportunity WHERE company_id = ? AND branch_id = ? AND stage = 'QUALIFIED'", scope),
                "proposal", count("SELECT count(*) FROM crm_opportunity WHERE company_id = ? AND branch_id = ? AND stage = 'PROPOSAL'", scope),
                "won", count("SELECT count(*) FROM crm_opportunity WHERE company_id = ? AND branch_id = ? AND stage = 'WON'", scope),
                "lost", count("SELECT count(*) FROM crm_opportunity WHERE company_id = ? AND branch_id = ? AND stage = 'LOST'", scope));
        return new DashboardSummary(start, end, salesTotal, salesCount, receivableOpen, payableOpen, lowStockCount, opportunities);
    }

    private BigDecimal amount(String sql, Scope scope, Object... tail) {
        Object[] args = args(scope, tail);
        BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
        return result == null ? BigDecimal.ZERO : result;
    }

    private long count(String sql, Scope scope, Object... tail) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class, args(scope, tail));
        return result == null ? 0 : result;
    }

    private Object[] args(Scope scope, Object[] tail) {
        Object[] args = new Object[2 + tail.length];
        args[0] = scope.companyId();
        args[1] = scope.branchId();
        System.arraycopy(tail, 0, args, 2, tail.length);
        return args;
    }

    private Scope requireDashboardAccess(Jwt jwt) {
        List<Scope> scopes = jdbcTemplate.query("""
                SELECT u.company_id, u.active_branch_id, u.role FROM app_user u
                JOIN user_branch_access uba ON uba.user_id = u.id AND uba.branch_id = u.active_branch_id
                JOIN branch b ON b.id = u.active_branch_id AND b.company_id = u.company_id AND b.active = true
                WHERE u.id = ? AND u.active = true
                """, (rs, rowNum) -> new Scope(rs.getObject("company_id", UUID.class),
                rs.getObject("active_branch_id", UUID.class), rs.getString("role")), UUID.fromString(jwt.getSubject()));
        if (scopes.isEmpty() || (!"ADMIN".equals(scopes.get(0).role()) && !"MANAGER".equals(scopes.get(0).role()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão para indicadores.");
        }
        return scopes.get(0);
    }

    public record DashboardSummary(LocalDate from, LocalDate to, BigDecimal salesTotal, long salesCount,
            BigDecimal receivableOpen, BigDecimal payableOpen, long lowStockCount,
            Map<String, Long> opportunities) {}
    private record Scope(UUID companyId, UUID branchId, String role) {}
}
