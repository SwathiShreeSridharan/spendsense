package com.spendsense.user.service;

import com.spendsense.security.CurrentUserService;
import com.spendsense.user.dto.UserProfileResponse;
import com.spendsense.user.exception.DuplicateEmailException;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserService userService;


    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterUserRequest request =
                new RegisterUserRequest(
                        "Swathi",
                        "swa@gmail.com",
                        "9876543210",
                        "MyPassword123"
                );

        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        User savedUser = new User(
                "Swathi",
                "swa@gmail.com",
                "9876543210",
                "$2a$10$hashedPassword"
        );

        savedUser.setUserId(userId);
        savedUser.setCreatedAt(createdAt);
        savedUser.setUpdatedAt(createdAt);

        when(userRepository.existsByEmail("swa@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("MyPassword123"))
                .thenReturn("$2a$10$hashedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response =
                userService.registerUser(request);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Swathi", response.getName());
        assertEquals("swa@gmail.com", response.getEmail());
        assertEquals(createdAt, response.getCreatedAt());

        verify(userRepository)
                .existsByEmail("swa@gmail.com");

        verify(passwordEncoder)
                .encode("MyPassword123");

        verify(userRepository)
                .save(any(User.class));
    }


    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterUserRequest request =
                new RegisterUserRequest(
                        "Swathi",
                        "swa@gmail.com",
                        "9876543210",
                        "MyPassword123"
                );

        when(userRepository.existsByEmail("swa@gmail.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.registerUser(request)
        );

        verify(userRepository)
                .existsByEmail("swa@gmail.com");

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void shouldReturnCurrentUserProfile() {
        UUID userId = UUID.randomUUID();

        User currentUser = new User(
                "Swathi",
                "swa@gmail.com",
                "9876543210",
                "hashed-password"
        );

        currentUser.setUserId(userId);

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);

        UserProfileResponse response =
                userService.getCurrentUserProfile();

        assertEquals(userId, response.getUserId());
        assertEquals("Swathi", response.getName());
        assertEquals("swa@gmail.com", response.getEmail());
        assertEquals(
                "9876543210",
                response.getMobileNumber()
        );

        verify(currentUserService).getCurrentUser();
    }
}
