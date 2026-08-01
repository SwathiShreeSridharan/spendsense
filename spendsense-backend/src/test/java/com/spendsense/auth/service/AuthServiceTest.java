package com.spendsense.auth.service;

import com.spendsense.auth.dto.LoginRequest;
import com.spendsense.auth.dto.LoginResponse;
import com.spendsense.user.exception.InvalidCredentialsException;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginSuccessfully(){
        LoginRequest request = new LoginRequest(
                "swa@gmail.com","MyPassword123"
        );

        User user = new User(
                "Swathi",
                "swa@gmail.com",
                "1234567890",
                "$2a$10$hashedPassword"
        );

        when(userRepository.findByEmail("swa@gmail.com")).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("MyPassword123","$2a$10$hashedPassword")).thenReturn(true);

        LoginResponse response = authService.login(request);

        assertNotNull(response);

        assertEquals("Swathi",response.getName());
        assertEquals("swa@gmail.com",response.getEmail());

        verify(userRepository)
                .findByEmail("swa@gmail.com");

        verify(passwordEncoder)
                .matches(
                        "MyPassword123",
                        "$2a$10$hashedPassword");
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotExist(){
        LoginRequest request = new LoginRequest(
                "unknown@gmail.com","MyPassword123"
        );

        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(userRepository).findByEmail("unknown@gmail.com");

        verify(passwordEncoder, never())
                .matches(anyString(),anyString());

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

        when(userRepository.findByEmail("swa@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "$2a$10$hashedPassword"
        )).thenReturn(false);


        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );


        verify(userRepository)
                .findByEmail("swa@gmail.com");

        verify(passwordEncoder)
                .matches(
                        "WrongPassword",
                        "$2a$10$hashedPassword"
                );
    }
}

