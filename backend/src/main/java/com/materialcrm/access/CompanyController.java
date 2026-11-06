package com.materialcrm.access;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/company")
public class CompanyController {
    private final JdbcTemplate jdbcTemplate;

    public CompanyController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPANY_READ')")
    public CompanyResponse current(Authentication authentication) {
        return jdbcTemplate.queryForObject(
                "select c.id, c.legal_name, c.trade_name, c.tax_id from company c "
                        + "join app_user u on u.company_id = c.id "
                        + "where lower(u.email) = lower(?) and u.active = true",
                (rs, rowNum) -> new CompanyResponse(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("legal_name"),
                        rs.getString("trade_name"),
                        rs.getString("tax_id")),
                authentication.getName());
    }

    public record CompanyResponse(UUID id, String legalName, String tradeName, String taxId) { }
}