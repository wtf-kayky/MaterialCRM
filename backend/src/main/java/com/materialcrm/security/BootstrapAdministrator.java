package com.materialcrm.security;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapAdministrator implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String companyName;
    private final String companyTaxId;
    private final String email;
    private final String password;

    public BootstrapAdministrator(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${materialcrm.bootstrap.company-name:}") String companyName,
            @Value("${materialcrm.bootstrap.company-tax-id:}") String companyTaxId,
            @Value("${materialcrm.bootstrap.admin-email:}") String email,
            @Value("${materialcrm.bootstrap.admin-password:}") String password) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.companyName = companyName;
        this.companyTaxId = companyTaxId;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (companyName.isBlank() || companyTaxId.isBlank() || email.isBlank() || password.isBlank()) {
            return;
        }
        if (jdbcTemplate.queryForObject("select count(*) from app_user", Integer.class) > 0) {
            return;
        }

        UUID companyId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        jdbcTemplate.update("insert into company (id, legal_name, trade_name, tax_id) values (?, ?, ?, ?)",
                companyId, companyName, companyName, companyTaxId);
        jdbcTemplate.update("insert into branch (id, company_id, name, code) values (?, ?, ?, ?)",
                branchId, companyId, "Matriz", "MATRIZ");
        jdbcTemplate.update("insert into role (id, company_id, name, description) values (?, ?, ?, ?)",
                roleId, companyId, "ADMINISTRADOR", "Acesso administrativo inicial");
        jdbcTemplate.update("insert into app_user (id, company_id, full_name, email, password_hash) values (?, ?, ?, ?, ?)",
                userId, companyId, "Administrador inicial", email, passwordEncoder.encode(password));
        jdbcTemplate.update("insert into user_role (user_id, role_id) values (?, ?)", userId, roleId);
        jdbcTemplate.update("insert into user_branch_access (user_id, branch_id) values (?, ?)", userId, branchId);
        jdbcTemplate.update("insert into role_permission (role_id, permission_code) select ?, code from permission", roleId);
        jdbcTemplate.update("insert into audit_event (id, company_id, branch_id, actor_user_id, event_type, resource_type, resource_id) values (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), companyId, branchId, userId, "BOOTSTRAP_ADMIN_CREATED", "APP_USER", userId);
    }
}