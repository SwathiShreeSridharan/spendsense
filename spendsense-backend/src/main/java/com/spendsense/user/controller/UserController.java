package com.spendsense.user.controller;

import com.spendsense.user.dto.RegisterUserRequest;
import com.spendsense.user.dto.UserProfileResponse;
import com.spendsense.user.dto.UserResponse;
import com.spendsense.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUserRequest request){

        UserResponse response = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        UserProfileResponse response =
                userService.getCurrentUserProfile();

        return ResponseEntity.ok(response);
    }

}
