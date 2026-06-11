package com.securegenai.gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — runs once per request.
 *
 * <p>Intercepts every HTTP request and:
 * <ol>
 *   <li>Reads the {@code Authorization: Bearer <token>} header.</li>
 *   <li>Extracts and validates the JWT via {@link JwtService}.</li>
 *   <li>Loads the {@link UserDetails} from {@link UserDetailsService}.</li>
 *   <li>Sets the authenticated principal in {@link SecurityContextHolder}.</li>
 * </ol>
 *
 * <p>If validation fails (missing, malformed, expired, or blacklisted token),
 * the filter passes the request on without setting authentication — causing
 * Spring Security's access-denied mechanism to reject it downstream.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTH_HEADER);

        // Skip if no Bearer token present
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            final String username = jwtService.extractUsername(jwt);

            // Only authenticate if not already authenticated in this request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                       // credentials are null for JWT
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("JWT authenticated [user={}] [path={}]",
                            username, request.getRequestURI());
                } else {
                    log.warn("JWT validation failed [user={}] [path={}]",
                            username, request.getRequestURI());
                }
            }

        } catch (Exception e) {
            // Log and continue — Spring Security will reject the unauthenticated request
            log.warn("JWT filter error [path={}]: {}", request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
