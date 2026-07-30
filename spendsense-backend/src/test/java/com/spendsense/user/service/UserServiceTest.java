package com.spendsense.user.service;

import com.spendsense.exception.DuplicateEmailException;
import com.spendsense.user.dto.RegisterUserRequest;
import com.spendsense.user.dto.UserResponse;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterUserRequest request = new RegisterUserRequest(
                "Swathi",
                "swa@gmail.com",
                "9876543210",
                "MyPassword123"
        );

        when(userRepository.findByEmail("swa@gmail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("MyPassword123"))
                .thenReturn("$2a$10$hashedPassword");

        User savedUser = new User(
                "Swathi",
                "swa@gmail.com",
                "9876543210",
                "$2a$10$hashedPassword"
        );

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response =
                userService.registerUser(request);

        assertNotNull(response);

        verify(userRepository)
                .findByEmail("swa@gmail.com");

        verify(passwordEncoder)
                .encode("MyPassword123");

        verify(userRepository)
                .save(any(User.class));
    }


    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        RegisterUserRequest request = new RegisterUserRequest(
                "Swathi",
                "swa@gmail.com",
                "9876543210",
                "MyPassword123"
        );

        User existingUser = new User(
                "Existing User",
                "swa@gmail.com",
                "9876543210",
                "existing-hashed-password"
        );

        when(userRepository.findByEmail("swa@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.registerUser(request)
        );

        verify(userRepository)
                .findByEmail("swa@gmail.com");

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }
}
