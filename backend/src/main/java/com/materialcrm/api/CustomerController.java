package com.materialcrm.api;

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
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final JdbcTemplate jdbcTemplate;

    public CustomerController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<CustomerView> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String search) {
        Scope scope = scope(jwt);
        String term = search == null ? "" : search.trim();
        return jdbcTemplate.query(
                "SELECT id, branch_id, customer_type, name, document, email, phone, active "
                        + "FROM customer WHERE company_id = ? AND (lower(name) LIKE lower(?) OR coalesce(document, '') LIKE ?) "
                        + "ORDER BY name LIMIT 100",
                (rs, rowNum) -> new CustomerView(rs.getObject("id", UUID.class), rs.getObject("branch_id", UUID.class),
                        rs.getString("customer_type"), rs.getString("name"), rs.getString("document"),
                        rs.getString("email"), rs.getString("phone"), rs.getBoolean("active")),
                scope.companyId(), "%" + term + "%", "%" + term + "%");
    }

    @PostMapping
    public CustomerView create(@RequestBody CustomerRequest request, @AuthenticationPrincipal Jwt jwt) {
        Scope scope = requireSalesAccess(jwt);
        validate(request);
        UUID branchId = validBranch(request.branchId(), scope);
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO customer (id, company_id, branch_id, customer_type, name, document, email, phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, scope.companyId(), branchId, request.customerType(), request.name().trim(), blankToNull(request.document()),
                blankToNull(request.email()), blankToNull(request.phone()));
        return find(id, scope.companyId());
    }

    @PutMapping("/{customerId}")
    public CustomerView update(@PathVariable UUID customerId, @RequestBody CustomerRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Scope scope = requireSalesAccess(jwt);
        validate(request);
        UUID branchId = validBranch(request.branchId(), scope);
        int updated = jdbcTemplate.update(
                "UPDATE customer SET branch_id = ?, customer_type = ?, name = ?, document = ?, email = ?, phone = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND company_id = ?",
                branchId, request.customerType(), request.name().trim(), blankToNull(request.document()),
                blankToNull(request.email()), blankToNull(request.phone()), customerId, scope.companyId());
        if (updated == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado.");
        return find(customerId, scope.companyId());
    }

    @DeleteMapping("/{customerId}")
    public void deactivate(@PathVariable UUID customerId, @AuthenticationPrincipal Jwt jwt) {
        Scope scope = requireSalesAccess(jwt);
        int updated = jdbcTemplate.update("UPDATE customer SET active = false, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND company_id = ?",
                customerId, scope.companyId());
        if (updated == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado.");
    }

    private CustomerView find(UUID id, UUID companyId) {
        return jdbcTemplate.queryForObject(
                "SELECT id, branch_id, customer_type, name, document, email, phone, active FROM customer WHERE id = ? AND company_id = ?",
                (rs, rowNum) -> new CustomerView(rs.getObject("id", UUID.class), rs.getObject("branch_id", UUID.class),
                        rs.getString("customer_type"), rs.getString("name"), rs.getString("document"),
                        rs.getString("email"), rs.getString("phone"), rs.getBoolean("active")), id, companyId);
    }

    private Scope requireSalesAccess(Jwt jwt) {
        Scope scope = scope(jwt);
        String role = scope.role();
        if (!role.equals("ADMIN") && !role.equals("MANAGER") && !role.equals("SELLER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão para clientes.");
        }
        return scope;
    }

    private Scope scope(Jwt jwt) {
        return jdbcTemplate.queryForObject(
                "SELECT company_id, active_branch_id, role FROM app_user WHERE id = ? AND active = true",
                (rs, rowNum) -> new Scope(rs.getObject("company_id", UUID.class), rs.getObject("active_branch_id", UUID.class),
                        rs.getString("role")), UUID.fromString(jwt.getSubject()));
    }

    private UUID validBranch(UUID branchId, Scope scope) {
        UUID selected = branchId == null ? scope.activeBranchId() : branchId;
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM branch WHERE id = ? AND company_id = ? AND active = true",
                Integer.class, selected, scope.companyId());
        if (count == null || count == 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filial inválida.");
        return selected;
    }

    private void validate(CustomerRequest request) {
        if (request.name() == null || request.name().isBlank()
                || request.customerType() == null || (!request.customerType().equals("PF") && !request.customerType().equals("PJ"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe nome e tipo de cliente válido.");
        }
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record CustomerRequest(String customerType, String name, String document, String email, String phone, UUID branchId) {}
    public record CustomerView(UUID id, UUID branchId, String customerType, String name, String document,
            String email, String phone, boolean active) {}
    private record Scope(UUID companyId, UUID activeBranchId, String role) {}
}
