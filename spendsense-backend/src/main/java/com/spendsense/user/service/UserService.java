package com.spendsense.user.service;

import com.spendsense.security.CurrentUserService;
import com.spendsense.user.exception.DuplicateEmailException;
import com.spendsense.user.dto.RegisterUserRequest;
import com.spendsense.user.dto.UserProfileResponse;
import com.spendsense.user.dto.UserResponse;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CurrentUserService currentUserService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    public UserResponse registerUser(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(
                    "Email is already registered"
            );
        }

        String hashedPassword =
                passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getMobileNumber(),
                hashedPassword
        );

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getUserId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getMobileNumber(),
                savedUser.getStatus(),
                savedUser.getCreatedAt()
        );
    }

    public UserProfileResponse getCurrentUserProfile(){
        User currentUser = currentUserService.getCurrentUser();

        return new UserProfileResponse(
                currentUser.getUserId(),
                currentUser.getName(),
                currentUser.getEmail(),
                currentUser.getMobileNumber()
        );
    }
}
