package com.spendsense.user.service;

import com.spendsense.exception.DuplicateEmailException;
import com.spendsense.user.dto.RegisterUserRequest;
import com.spendsense.user.dto.UserProfileResponse;
import com.spendsense.user.dto.UserResponse;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse registerUser(RegisterUserRequest request){
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if(existingUser.isPresent()){
            throw new DuplicateEmailException("Email is already registered");
        }
        String hashedPassword = hashPassword(request.getPassword());

        User user = new User(request.getName(), request.getEmail(), request.getMobileNumber(), hashedPassword);

        User savedUser = userRepository.save(user);

        return new UserResponse(user.getUserId(),user.getName(),user.getEmail(),
                user.getMobileNumber(),user.getStatus(),user.getCreatedAt());
    }

    public UserProfileResponse getCurrentUser(User user){
        return new UserProfileResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNumber()
        );
    }

    private String hashPassword(String password){
        return passwordEncoder.encode(password);
    }
}
