package com.greenboard.investman.multitenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Ensures all existing tenant schemas are migrated to the latest Flyway tenant scripts
 * on application startup, before any web or API traffic is handled.
 */
@Component
@Order(10)
public class TenantMigrationStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TenantMigrationStartupRunner.class);

    private final TenantProvisioningService tenantProvisioningService;

    public TenantMigrationStartupRunner(TenantProvisioningService tenantProvisioningService) {
        this.tenantProvisioningService = tenantProvisioningService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Checking and applying pending database migrations across all tenant schemas...");
        try {
            tenantProvisioningService.migrateAllTenants();
        } catch (Exception e) {
            log.error("Error executing tenant migrations on application startup: {}", e.getMessage(), e);
        }
    }
}
