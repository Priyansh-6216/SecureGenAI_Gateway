package com.securegenai.gateway.security;

/**
 * Platform roles for SecureGenAI Gateway.
 *
 * <ul>
 *   <li>{@code ADMIN}            — Full platform access: manage users, view all analytics, configure policies.</li>
 *   <li>{@code SECURITY_ANALYST} — Can submit prompts, view audit logs, run validation checks.</li>
 *   <li>{@code EMPLOYEE}         — Can submit prompts only; no access to admin or audit views.</li>
 * </ul>
 *
 * Spring Security uses the {@code ROLE_} prefix convention.
 * These values are stored in JWT claims as bare strings (e.g., "ADMIN")
 * and are expanded to "ROLE_ADMIN" by {@link UserDetailsServiceImpl}.
 */
public enum UserRole {

    ADMIN,
    SECURITY_ANALYST,
    EMPLOYEE;

    /**
     * Returns the Spring Security authority string (e.g., {@code "ROLE_ADMIN"}).
     */
    public String toAuthority() {
        return "ROLE_" + this.name();
    }
}
