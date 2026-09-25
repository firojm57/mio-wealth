package com.greenboard.investman.multitenancy;

import com.greenboard.investman.repository.user.UserRepository;
import com.greenboard.investman.vo.user.UserProfileVO;
import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

@Service
public class TenantProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(TenantProvisioningService.class);

    private final DataSource dataSource;
    private final UserRepository userRepository;

    public TenantProvisioningService(DataSource dataSource, @Lazy UserRepository userRepository) {
        this.dataSource = dataSource;
        this.userRepository = userRepository;
    }

    public void provisionTenant(String schemaName) {
        migrateTenant(schemaName);
    }

    public void migrateTenant(String schemaName) {
        if (StringUtils.isBlank(schemaName) || !schemaName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid schema name: " + schemaName);
        }

        // 1. Physically ensure the schema exists in PostgreSQL (DDL)
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS \"" + schemaName + "\"");
            log.info("Ensured PostgreSQL schema '{}' exists", schemaName);
        } catch (SQLException e) {
            log.error("Failed to create/ensure schema '{}': {}", schemaName, e.getMessage(), e);
            throw new IllegalStateException("Failed to create database schema: " + schemaName, e);
        }

        // 2. Programmatically apply tenant migrations to the schema via Flyway
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations("classpath:db/migration/tenants")
                    .baselineOnMigrate(false)
                    .load();

            flyway.migrate();
            log.info("Successfully executed tenant Flyway migrations for schema '{}'", schemaName);
        } catch (Exception e) {
            log.error("Failed to run Flyway migration for tenant schema '{}': {}", schemaName, e.getMessage(), e);
            throw new IllegalStateException("Failed to migrate tenant schema: " + schemaName, e);
        }
    }

    public void migrateAllTenants() {
        log.info("Verifying Flyway migrations for all registered tenant schemas...");
        List<String> tenantSchemas = userRepository.findAllTenantSchemas();
        if (CollectionUtils.isEmpty(tenantSchemas)) {
            log.info("No tenant schemas found to migrate.");
            return;
        }

        for (String schema : tenantSchemas) {
            if (StringUtils.isNotBlank(schema)) {
                try {
                    migrateTenant(schema.trim());
                } catch (Exception e) {
                    log.error("Failed to run migration for tenant schema '{}': {}", schema, e.getMessage());
                }
            }
        }
        log.info("Tenant schema migration check completed for {} schema(s).", tenantSchemas.size());
    }

    public void initTenantProfile(String schemaName, String firstName, String middleName, String lastName, String email, String mobile) {
        String sql = "INSERT INTO \"" + schemaName + "\".user_profile (id, first_name, middle_name, last_name, email_id, mobile_number, created_on, updated_on, last_login) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW(), NOW())";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, firstName);
            ps.setString(3, middleName);
            ps.setString(4, lastName);
            ps.setString(5, email);
            ps.setString(6, mobile);
            ps.executeUpdate();
            log.info("Initialized default profile in schema '{}' for email '{}'", schemaName, email);
        } catch (SQLException e) {
            log.error("Failed to insert initial profile in schema '{}': {}", schemaName, e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize profile in tenant schema: " + schemaName, e);
        }
    }

    public UserProfileVO getTenantProfile(String schemaName) {
        String sql = "SELECT first_name, middle_name, last_name, email_id, mobile_number FROM \"" + schemaName + "\".user_profile LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                UserProfileVO vo = new UserProfileVO();
                vo.setFirstName(rs.getString("first_name"));
                vo.setMiddleName(rs.getString("middle_name"));
                vo.setLastName(rs.getString("last_name"));
                vo.setEmail(rs.getString("email_id"));
                vo.setMobile(rs.getString("mobile_number"));
                return vo;
            }
        } catch (SQLException e) {
            log.warn("Could not query profile from schema '{}': {}", schemaName, e.getMessage());
        }
        return null;
    }
}
