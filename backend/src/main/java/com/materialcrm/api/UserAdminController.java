package com.materialcrm.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {
    private static final Set<String> ROLES = Set.of("ADMIN", "MANAGER", "SELLER", "CASHIER", "STOCK", "USER");

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public UserAdminController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UserView> list(@AuthenticationPrincipal Jwt jwt) {
        UUID companyId = requireManager(jwt);
        return jdbcTemplate.query(
                "SELECT id, email, full_name, active, role, active_branch_id FROM app_user WHERE company_id = ? ORDER BY full_name, email",
                (rs, rowNum) -> new UserView(rs.getObject("id", UUID.class), rs.getString("email"),
                        rs.getString("full_name"), rs.getBoolean("active"), rs.getString("role"),
                        rs.getObject("active_branch_id", UUID.class)), companyId);
    }

    @PostMapping
    public UserView create(@RequestBody CreateUserRequest request, @AuthenticationPrincipal Jwt jwt) {
        UUID companyId = requireManager(jwt);
        validate(request.email(), request.password(), request.fullName(), request.role());
        UUID branchId = validBranch(request.branchId(), companyId);
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO app_user (id, email, password_hash, active, role, company_id, active_branch_id, full_name) VALUES (?, lower(?), ?, true, ?, ?, ?, ?)",
                id, request.email().trim(), passwordEncoder.encode(request.password()), request.role(), companyId, branchId,
                request.fullName().trim());
        jdbcTemplate.update("INSERT INTO user_branch_access (user_id, branch_id) VALUES (?, ?)", id, branchId);
        return new UserView(id, request.email().trim().toLowerCase(), request.fullName().trim(), true, request.role(), branchId);
    }

    @PutMapping("/{userId}")
    public UserView update(@PathVariable UUID userId, @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID companyId = requireManager(jwt);
        if (request.role() != null && !ROLES.contains(request.role())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil de acesso inválido.");
        }
        int updated = jdbcTemplate.update(
                "UPDATE app_user SET full_name = COALESCE(?, full_name), role = COALESCE(?, role), active = COALESCE(?, active) WHERE id = ? AND company_id = ?",
                blankToNull(request.fullName()), request.role(), request.active(), userId, companyId);
        if (updated == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        return jdbcTemplate.queryForObject(
                "SELECT id, email, full_name, active, role, active_branch_id FROM app_user WHERE id = ?",
                (rs, rowNum) -> new UserView(rs.getObject("id", UUID.class), rs.getString("email"),
                        rs.getString("full_name"), rs.getBoolean("active"), rs.getString("role"),
                        rs.getObject("active_branch_id", UUID.class)), userId);
    }

    private UUID requireManager(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão administrativa.");
        }
        return jdbcTemplate.queryForObject("SELECT company_id FROM app_user WHERE id = ? AND active = true",
                UUID.class, UUID.fromString(jwt.getSubject()));
    }

    private UUID validBranch(UUID branchId, UUID companyId) {
        if (branchId == null) {
            return jdbcTemplate.queryForObject("SELECT id FROM branch WHERE company_id = ? AND active = true ORDER BY code LIMIT 1",
                    UUID.class, companyId);
        }
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM branch WHERE id = ? AND company_id = ? AND active = true",
                Integer.class, branchId, companyId);
        if (count == null || count == 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filial inválida.");
        return branchId;
    }

    private void validate(String email, String password, String fullName, String role) {
        if (email == null || !email.contains("@") || password == null || password.length() < 12
                || fullName == null || fullName.isBlank() || role == null || !ROLES.contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados do usuário inválidos.");
        }
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record CreateUserRequest(String email, String password, String fullName, String role, UUID branchId) {}
    public record UpdateUserRequest(String fullName, String role, Boolean active) {}
    public record UserView(UUID id, String email, String fullName, boolean active, String role, UUID activeBranchId) {}
}
