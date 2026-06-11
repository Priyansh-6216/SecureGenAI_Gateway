package com.securegenai.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * In-memory {@link UserDetailsService} with three hardcoded demo users — one per platform role.
 *
 * <p><strong>Demo Credentials:</strong>
 * <table border="1">
 *   <tr><th>Username</th><th>Password</th><th>Role</th></tr>
 *   <tr><td>admin</td><td>admin123</td><td>ADMIN</td></tr>
 *   <tr><td>analyst</td><td>analyst123</td><td>SECURITY_ANALYST</td></tr>
 *   <tr><td>employee</td><td>emp123</td><td>EMPLOYEE</td></tr>
 * </table>
 *
 * <p><strong>Day 4 Migration:</strong> This class will be replaced by a JPA-backed
 * implementation that loads users from the PostgreSQL {@code users} table.
 *
 * <p>Passwords are BCrypt-encoded at class load time so the hashes match
 * the configured {@link org.springframework.security.crypto.password.PasswordEncoder}.
 */
@Slf4j
@Service
@Primary
public class UserDetailsServiceImpl implements UserDetailsService {

    // Pre-compute BCrypt hashes once at startup (strength 12 matches SecurityConfig)
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(12);

    private static final Map<String, UserDetails> USERS = Map.of(
            "admin", buildUser("admin", "admin123", UserRole.ADMIN),
            "analyst", buildUser("analyst", "analyst123", UserRole.SECURITY_ANALYST),
            "employee", buildUser("employee", "emp123", UserRole.EMPLOYEE)
    );

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = USERS.get(username.toLowerCase());
        if (user == null) {
            log.warn("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        log.debug("Loaded user: {} with role: {}", username, user.getAuthorities());
        return user;
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private static UserDetails buildUser(String username, String rawPassword, UserRole role) {
        return User.builder()
                .username(username)
                .password(ENCODER.encode(rawPassword))
                .authorities(List.of(new SimpleGrantedAuthority(role.toAuthority())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
