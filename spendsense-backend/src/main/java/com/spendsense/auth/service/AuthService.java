package com.spendsense.auth.service;

import com.spendsense.auth.dto.LoginRequest;
import com.spendsense.auth.dto.LoginResponse;
import com.spendsense.security.JwtService;
import com.spendsense.user.entity.User;
import com.spendsense.user.exception.InvalidCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request){
        Authentication authentication;

        try {
            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmail(),
                                    request.getPassword()
                            )
                    );
        }
        catch (AuthenticationException exception) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }
        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                token
        );
    }
}
