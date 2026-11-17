package com.materialcrm.security;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseUserDetailsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        var users = jdbcTemplate.query(
                "select id, email, password_hash, active from app_user where lower(email) = lower(?)",
                (rs, rowNum) -> new StoredUser(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getBoolean("active")),
                email);

        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Credenciais inválidas");
        }

        var user = users.getFirst();
        List<String> permissions = jdbcTemplate.queryForList(
                "select distinct rp.permission_code from user_role ur "
                        + "join role_permission rp on rp.role_id = ur.role_id "
                        + "where ur.user_id = ?",
                String.class,
                user.id());

        return User.withUsername(user.email())
                .password(user.passwordHash())
                .disabled(!user.active())
                .authorities(permissions.toArray(String[]::new))
                .build();
    }

    private record StoredUser(UUID id, String email, String passwordHash, boolean active) { }
}