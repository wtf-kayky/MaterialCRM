package com.materialcrm.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/organization")
public class OrganizationController {
    private final JdbcTemplate jdbcTemplate;

    public OrganizationController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/context")
    public OrganizationContext context(@AuthenticationPrincipal Jwt jwt) {
        UserScope scope = scope(jwt);
        Company company = jdbcTemplate.queryForObject(
                "SELECT id, legal_name, trade_name, tax_id, active FROM company WHERE id = ?",
                (rs, rowNum) -> new Company(rs.getObject("id", UUID.class), rs.getString("legal_name"),
                        rs.getString("trade_name"), rs.getString("tax_id"), rs.getBoolean("active")),
                scope.companyId());
        List<Branch> branches = jdbcTemplate.query(
                "SELECT id, name, code, active FROM branch WHERE company_id = ? ORDER BY name",
                (rs, rowNum) -> new Branch(rs.getObject("id", UUID.class), rs.getString("name"),
                        rs.getString("code"), rs.getBoolean("active"), rs.getObject("id", UUID.class).equals(scope.activeBranchId())),
                scope.companyId());
        return new OrganizationContext(company, branches, scope.role());
    }

    @PostMapping("/companies")
    public Company createCompany(@RequestBody CompanyRequest request, @AuthenticationPrincipal Jwt jwt) {
        requireManagement(jwt);
        if (request.legalName() == null || request.legalName().isBlank() || request.tradeName() == null
                || request.tradeName().isBlank() || request.taxId() == null || request.taxId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe razão social, nome fantasia e CNPJ.");
        }
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO company (id, legal_name, trade_name, tax_id) VALUES (?, ?, ?, ?)", id,
                request.legalName().trim(), request.tradeName().trim(), request.taxId().trim());
        return new Company(id, request.legalName().trim(), request.tradeName().trim(), request.taxId().trim(), true);
    }

    @PostMapping("/branches")
    public Branch createBranch(@RequestBody BranchRequest request, @AuthenticationPrincipal Jwt jwt) {
        UserScope scope = requireManagement(jwt);
        if (request.name() == null || request.name().isBlank() || request.code() == null || request.code().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe nome e código da filial.");
        }
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO branch (id, company_id, name, code) VALUES (?, ?, ?, ?)", id, scope.companyId(),
                request.name().trim(), request.code().trim().toUpperCase());
        return new Branch(id, request.name().trim(), request.code().trim().toUpperCase(), true, false);
    }

    @PutMapping("/branches/{branchId}/active")
    public Map<String, Object> selectBranch(@PathVariable UUID branchId, @AuthenticationPrincipal Jwt jwt) {
        UserScope scope = scope(jwt);
        if (!branchBelongsToCompany(branchId, scope.companyId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Filial não encontrada para este usuário.");
        }
        int updated = jdbcTemplate.update("UPDATE app_user SET active_branch_id = ? WHERE id = ? AND company_id = ?",
                branchId, UUID.fromString(jwt.getSubject()), scope.companyId());
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Filial não encontrada para este usuário.");
        }
        return Map.of("activeBranchId", branchId);
    }

    private boolean branchBelongsToCompany(UUID branchId, UUID companyId) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM branch WHERE id = ? AND company_id = ?",
                Integer.class, branchId, companyId);
        return count != null && count == 1;
    }

    private UserScope requireManagement(Jwt jwt) {
        UserScope scope = scope(jwt);
        if (!scope.role().equals("ADMIN") && !scope.role().equals("MANAGER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão para administrar a organização.");
        }
        return scope;
    }

    private UserScope scope(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return jdbcTemplate.queryForObject(
                "SELECT company_id, active_branch_id, role FROM app_user WHERE id = ? AND active = true",
                (rs, rowNum) -> new UserScope(rs.getObject("company_id", UUID.class),
                        rs.getObject("active_branch_id", UUID.class), rs.getString("role")), userId);
    }

    public record OrganizationContext(Company company, List<Branch> branches, String role) {}
    public record Company(UUID id, String legalName, String tradeName, String taxId, boolean active) {}
    public record Branch(UUID id, String name, String code, boolean active, boolean selected) {}
    public record CompanyRequest(String legalName, String tradeName, String taxId) {}
    public record BranchRequest(String name, String code) {}
    private record UserScope(UUID companyId, UUID activeBranchId, String role) {}
}
