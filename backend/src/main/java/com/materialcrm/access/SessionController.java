package com.materialcrm.access;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/session")
public class SessionController {
    private final JdbcTemplate jdbcTemplate;

    public SessionController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPANY_READ')")
    public SessionResponse current(Authentication authentication) {
        var user = jdbcTemplate.queryForObject(
                "select u.id as user_id, u.full_name, u.email, c.id as company_id, c.trade_name "
                        + "from app_user u join company c on c.id = u.company_id "
                        + "where lower(u.email) = lower(?) and u.active = true",
                (rs, rowNum) -> new UserContext(
                        UUID.fromString(rs.getString("user_id")),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        UUID.fromString(rs.getString("company_id")),
                        rs.getString("trade_name")),
                authentication.getName());

        List<BranchResponse> branches = jdbcTemplate.query(
                "select b.id, b.name, b.code from user_branch_access uba "
                        + "join branch b on b.id = uba.branch_id "
                        + "where uba.user_id = ? and b.active = true order by b.name",
                (rs, rowNum) -> new BranchResponse(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getString("code")),
                user.id());

        return new SessionResponse(user.fullName(), user.email(), user.companyId(), user.tradeName(), branches);
    }

    public record SessionResponse(String fullName, String email, UUID companyId, String companyName,
                                  List<BranchResponse> branches) { }

    public record BranchResponse(UUID id, String name, String code) { }

    private record UserContext(UUID id, String fullName, String email, UUID companyId, String tradeName) { }
}