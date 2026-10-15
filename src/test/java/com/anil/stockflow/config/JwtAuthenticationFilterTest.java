
package com.anil.stockflow.config;

import com.anil.stockflow.entity.User;
import com.anil.stockflow.repository.UserRepository;
import com.anil.stockflow.service.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserRepository userRepository;
    private JwtAuthenticationFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        filter = new JwtAuthenticationFilter(jwtService, userRepository);
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsMissing() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsNotBearer() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    void shouldReturn401WhenTokenIsInvalid() throws Exception {
        var request = requestWithToken("bad-token");
        var response = new MockHttpServletResponse();

        when(jwtService.extractUsername("bad-token")).thenReturn("anil");
        when(jwtService.isTokenValid("bad-token", "anil")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldReturn401WhenUserDoesNotExist() throws Exception {
        var request = requestWithToken("valid-token");
        var response = new MockHttpServletResponse();

        when(jwtService.extractUsername("valid-token")).thenReturn("anil");
        when(jwtService.isTokenValid("valid-token", "anil")).thenReturn(true);
        when(userRepository.findByUsername("anil")).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldReturn403WhenUserRoleIsMissing() throws Exception {
        var request = requestWithToken("valid-token");
        var response = new MockHttpServletResponse();
        User user = userWithRole(null);

        when(jwtService.extractUsername("valid-token")).thenReturn("anil");
        when(jwtService.isTokenValid("valid-token", "anil")).thenReturn(true);
        when(userRepository.findByUsername("anil")).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldReturn403WhenUserRoleIsBlank() throws Exception {
        var request = requestWithToken("valid-token");
        var response = new MockHttpServletResponse();
        User user = userWithRole("   ");

        when(jwtService.extractUsername("valid-token")).thenReturn("anil");
        when(jwtService.isTokenValid("valid-token", "anil")).thenReturn(true);
        when(userRepository.findByUsername("anil")).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        assertEquals(403, response.getStatus());
    }

    @Test
    void shouldContinueWhenTokenIsValidAndRoleHasPrefix() throws Exception {
        var request = requestWithToken("valid-token");
        var response = new MockHttpServletResponse();
        User user = userWithRole("ROLE_ADMIN");

        when(jwtService.extractUsername("valid-token")).thenReturn("anil");
        when(jwtService.isTokenValid("valid-token", "anil")).thenReturn(true);
        when(userRepository.findByUsername("anil")).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals("ROLE_ADMIN", SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().iterator().next().getAuthority());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldAddRolePrefixWhenRoleDoesNotHavePrefix() throws Exception {
        var request = requestWithToken("valid-token");
        var response = new MockHttpServletResponse();
        User user = userWithRole("USER");

        when(jwtService.extractUsername("valid-token")).thenReturn("anil");
        when(jwtService.isTokenValid("valid-token", "anil")).thenReturn(true);
        when(userRepository.findByUsername("anil")).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        assertEquals("ROLE_USER", SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldReturn401WhenJwtServiceThrowsIllegalArgumentException() throws Exception {
        var request = requestWithToken("bad-token");
        var response = new MockHttpServletResponse();

        when(jwtService.extractUsername("bad-token"))
                .thenThrow(new IllegalArgumentException("Bad token"));

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldNotReplaceExistingAuthentication() throws Exception {
        var request = requestWithToken("valid-token");
        var response = new MockHttpServletResponse();
        User user = userWithRole("USER");

        var existing = new org.springframework.security.authentication
                .UsernamePasswordAuthenticationToken(
                "existing-user", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);

        when(jwtService.extractUsername("valid-token")).thenReturn("anil");
        when(jwtService.isTokenValid("valid-token", "anil")).thenReturn(true);
        when(userRepository.findByUsername("anil")).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        assertSame(existing, SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    private MockHttpServletRequest requestWithToken(String token) {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private User userWithRole(String role) {
        User user = new User("anil", "anil@example.com", "encoded-password", role);
        user.setId(1L);
        return user;
    }
}
