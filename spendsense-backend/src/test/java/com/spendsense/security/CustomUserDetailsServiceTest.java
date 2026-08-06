package com.spendsense.security;

import com.spendsense.user.entity.User;
import com.spendsense.user.entity.UserStatus;
import com.spendsense.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void shouldLoadActiveUserByEmail() {
        User user = new User(
                "Swathi",
                "swathi@gmail.com",
                "9876543210",
                "hashed-password"
        );

        when(userRepository.findByEmailAndStatus(
                "swathi@gmail.com",
                UserStatus.ACTIVE
        )).thenReturn(Optional.of(user));

        UserDetails result =
                userDetailsService.loadUserByUsername(
                        "swathi@gmail.com"
                );

        assertSame(user, result);

        verify(userRepository)
                .findByEmailAndStatus(
                        "swathi@gmail.com",
                        UserStatus.ACTIVE
                );
    }

    @Test
    void shouldThrowWhenActiveUserDoesNotExist() {
        when(userRepository.findByEmailAndStatus(
                "inactive@gmail.com",
                UserStatus.ACTIVE
        )).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService
                        .loadUserByUsername(
                                "inactive@gmail.com"
                        )
        );

        verify(userRepository)
                .findByEmailAndStatus(
                        "inactive@gmail.com",
                        UserStatus.ACTIVE
                );
    }
}