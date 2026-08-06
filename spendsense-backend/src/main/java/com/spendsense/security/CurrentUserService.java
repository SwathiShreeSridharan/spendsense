package com.spendsense.security;

import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || authentication.getName() == null) {

            throw new AuthenticationCredentialsNotFoundException(
                    "Authentication is required"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(
                        () -> new AuthenticationCredentialsNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }
}