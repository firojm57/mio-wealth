package com.greenboard.investman.security;

import com.greenboard.investman.model.user.User;
import com.greenboard.investman.multitenancy.TenantContext;
import com.greenboard.investman.repository.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, @Lazy UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String bearerToken = request.getHeader("Authorization");

            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String jwt = bearerToken.substring(7).trim();

                // 1. Verify cryptographic validity & expiration
                if (StringUtils.isBlank(jwt) || !tokenProvider.validateToken(jwt)) {
                    log.warn("Rejected unauthorized request: Invalid, tampered, or expired JWT token for [{} {}]",
                            request.getMethod(), request.getRequestURI());
                    sendUnauthorizedResponse(response, "Invalid, tampered, or expired authentication token. Please sign in again.");
                    return;
                }

                String username = tokenProvider.getUsernameFromToken(jwt);
                String tenant = tokenProvider.getTenantFromToken(jwt);

                // 2. Verify user and tenant existence in database
                Optional<User> userOpt = userRepository.findByUserId(username);
                if (userOpt.isEmpty() || !StringUtils.equals(userOpt.get().getTenantSchema(), tenant)) {
                    log.warn("Rejected unauthorized request: User '{}' does not exist or tenant '{}' is invalid for [{} {}]",
                            username, tenant, request.getMethod(), request.getRequestURI());
                    sendUnauthorizedResponse(response, "Unauthorized session: User or tenant mapping is invalid. Please sign in again.");
                    return;
                }

                // 3. Establish tenant context and Spring Security authentication
                TenantContext.setTenantId(tenant);
                log.debug("Authenticated user '{}' on tenant '{}' for [{} {}]",
                        username, tenant, request.getMethod(), request.getRequestURI());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("Could not set user authentication or tenant in security context", ex);
            sendUnauthorizedResponse(response, "Authentication processing failed. Please sign in again.");
        } finally {
            TenantContext.clear();
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String json = String.format("{\"status\":401,\"message\":\"%s\"}", message);
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
