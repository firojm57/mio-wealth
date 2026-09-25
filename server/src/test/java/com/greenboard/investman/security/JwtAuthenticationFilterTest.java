package com.greenboard.investman.security;

import com.greenboard.investman.model.user.User;
import com.greenboard.investman.multitenancy.TenantContext;
import com.greenboard.investman.repository.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        filter = new JwtAuthenticationFilter(tokenProvider, userRepository);
        responseWriter = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateValidTokenWithValidUserAndTenant() throws ServletException, IOException {
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("john_doe");
        when(tokenProvider.getTenantFromToken(token)).thenReturn("tenant_123");

        User user = new User("john_doe", "hashed_pass", "tenant_123");
        when(userRepository.findByUserId("john_doe")).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void shouldRejectInvalidOrTamperedTokenWith401() throws ServletException, IOException {
        String token = "tampered.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(responseWriter.toString().contains("Invalid, tampered, or expired"));
    }

    @Test
    void shouldRejectWhenUserDoesNotExistInDatabase() throws ServletException, IOException {
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("ghost_user");
        when(tokenProvider.getTenantFromToken(token)).thenReturn("tenant_ghost");
        when(userRepository.findByUserId("ghost_user")).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(responseWriter.toString().contains("Unauthorized session"));
    }

    @Test
    void shouldRejectWhenTenantDoesNotMatchUserRecord() throws ServletException, IOException {
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("john_doe");
        when(tokenProvider.getTenantFromToken(token)).thenReturn("tenant_attacker");

        User user = new User("john_doe", "hashed_pass", "tenant_real");
        when(userRepository.findByUserId("john_doe")).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(responseWriter.toString().contains("Unauthorized session"));
    }

    @Test
    void shouldPassThroughWhenNoTokenPresent() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
}
