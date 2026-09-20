package com.greenboard.investman.multitenancy;

public final class TenantContext {

    public static final String DEFAULT_TENANT = "public";

    private static final ThreadLocal<String> CURRENT_TENANT = ThreadLocal.withInitial(() -> DEFAULT_TENANT);

    private TenantContext() {
        // Utility class
    }

    public static void setTenantId(String tenantId) {
        if (tenantId != null && !tenantId.isBlank()) {
            CURRENT_TENANT.set(tenantId.trim());
        } else {
            CURRENT_TENANT.set(DEFAULT_TENANT);
        }
    }

    public static String getTenantId() {
        String tenant = CURRENT_TENANT.get();
        return (tenant != null && !tenant.isBlank()) ? tenant : DEFAULT_TENANT;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
