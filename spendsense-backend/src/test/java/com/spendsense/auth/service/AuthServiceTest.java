package com.spendsense.auth.service;

import com.spendsense.auth.dto.LoginRequest;
import com.spendsense.auth.dto.LoginResponse;
import com.spendsense.security.JwtService;
import com.spendsense.user.exception.InvalidCredentialsException;
import com.spendsense.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest(
                "swa@gmail.com",
                "MyPassword123"
        );

        User user = new User(
                "Swathi",
                "swa@gmail.com",
                "1234567890",
                "$2a$10$hashedPassword"
        );

        user.setUserId(UUID.randomUUID());

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn("jwt-token");

        LoginResponse response =
                authService.login(request);

        assertNotNull(response);
        assertEquals("Swathi", response.getName());
        assertEquals("swa@gmail.com", response.getEmail());
        assertEquals("jwt-token", response.getToken());

        verify(authenticationManager)
                .authenticate(any());

        verify(jwtService)
                .generateToken(user);
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotExist(){
        LoginRequest request = new LoginRequest(
                "unknown@gmail.com","MyPassword123"
        );

        User user = new User(
                "Swathi",
                "swa@gmail.com",
                "1234567890",
                "$2a$10$hashedPassword");

        user.setUserId(UUID.randomUUID());

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(user);

        when(jwtService.generateToken(user))
                .thenReturn("jwt-token");

        LoginResponse response =
                authService.login(request);

        assertNotNull(response);
        assertEquals("Swathi", response.getName());
        assertEquals("swa@gmail.com", response.getEmail());
        assertEquals("jwt-token", response.getToken());

        verify(authenticationManager)
                .authenticate(any());

        verify(jwtService)
                .generateToken(user);

    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest(
                "swa@gmail.com",
                "WrongPassword"
        );

        User user = new User(
                "Swathi",
                "swa@gmail.com",
                "9876543210",
                "$2a$10$hashedPassword"
        );

        when(authenticationManager.authenticate(any()))
                .thenThrow(
                        new BadCredentialsException(
                                "Bad credentials"
                        )
                );

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never())
                .generateToken(any());
    }
}

