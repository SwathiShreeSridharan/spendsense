package com.spendsense.config;

import com.spendsense.security.CustomUserDetailsService;
import com.spendsense.security.JwtAuthenticationFilter;
import com.spendsense.security.JwtService;
import com.spendsense.user.controller.UserController;
import com.spendsense.user.dto.RegisterUserRequest;
import com.spendsense.user.dto.UserProfileResponse;
import com.spendsense.user.dto.UserResponse;
import com.spendsense.user.entity.User;
import com.spendsense.user.entity.UserStatus;
import com.spendsense.user.service.UserService;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void shouldAllowRegistrationWithoutToken()
            throws Exception {

        UserResponse response =
                new UserResponse(
                        UUID.randomUUID(),
                        "Swathi",
                        "swathi@gmail.com",
                        "9876543210",
                        UserStatus.ACTIVE,
                        LocalDateTime.now()
                );

        when(userService.registerUser(
                any(RegisterUserRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/users")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "name": "Swathi",
                                          "email": "swathi@gmail.com",
                                          "mobileNumber": "9876543210",
                                          "password": "MyPassword123"
                                        }
                                        """)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectProtectedEndpointWithoutToken()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/users/me")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldAllowProtectedEndpointWithValidToken()
            throws Exception {

        UUID userId = UUID.randomUUID();

        User user = new User(
                "Swathi",
                "swathi@gmail.com",
                "9876543210",
                "hashed-password"
        );

        user.setUserId(userId);

        UserProfileResponse response =
                new UserProfileResponse(
                        userId,
                        "Swathi",
                        "swathi@gmail.com",
                        "9876543210"
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

        when(userService.getCurrentUserProfile())
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/users/me")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer valid-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Swathi"))
                .andExpect(jsonPath("$.email")
                        .value("swathi@gmail.com"));

        verify(jwtService)
                .extractUsername("valid-token");

        verify(jwtService)
                .isTokenValid("valid-token", user);

        verify(userService)
                .getCurrentUserProfile();
    }

    @Test
    void shouldRejectProtectedEndpointWithMalformedToken()
            throws Exception {

        when(jwtService.extractUsername("malformed-token"))
                .thenThrow(
                        new MalformedJwtException(
                                "Invalid JWT"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/users/me")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer malformed-token"
                                )
                )
                .andExpect(status().isUnauthorized());

        verify(jwtService)
                .extractUsername("malformed-token");

        verifyNoInteractions(
                userDetailsService,
                userService
        );
    }
}