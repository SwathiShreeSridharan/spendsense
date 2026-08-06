package com.spendsense.security;

import com.spendsense.user.entity.User;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(
                jwtService,
                userDetailsService
        );
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer valid-token"
        );

        User user = new User(
                "Swathi",
                "swathi@gmail.com",
                "9876543210",
                "hashed-password"
        );

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("swathi@gmail.com");

        when(userDetailsService.loadUserByUsername(
                "swathi@gmail.com"
        )).thenReturn(user);

        when(jwtService.isTokenValid(
                "valid-token",
                user
        )).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        assertEquals(
                "swathi@gmail.com",
                authentication.getName()
        );

        assertSame(
                user,
                authentication.getPrincipal()
        );

        verify(jwtService)
                .extractUsername("valid-token");

        verify(userDetailsService)
                .loadUserByUsername("swathi@gmail.com");

        verify(jwtService)
                .isTokenValid("valid-token", user);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldContinueWithoutAuthenticationWhenTokenIsMalformed()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer malformed-token"
        );

        when(jwtService.extractUsername("malformed-token"))
                .thenThrow(
                        new MalformedJwtException(
                                "Invalid JWT"
                        )
                );

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .extractUsername("malformed-token");

        verifyNoInteractions(userDetailsService);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateUserWhenTokenIsInvalid()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer invalid-token"
        );

        User user = new User(
                "Swathi",
                "swathi@gmail.com",
                "9876543210",
                "hashed-password"
        );

        when(jwtService.extractUsername("invalid-token"))
                .thenReturn("swathi@gmail.com");

        when(userDetailsService.loadUserByUsername(
                "swathi@gmail.com"
        )).thenReturn(user);

        when(jwtService.isTokenValid(
                "invalid-token",
                user
        )).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .extractUsername("invalid-token");

        verify(userDetailsService)
                .loadUserByUsername("swathi@gmail.com");

        verify(jwtService)
                .isTokenValid("invalid-token", user);

        verify(filterChain)
                .doFilter(request, response);
    }
}