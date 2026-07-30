package com.spendsense.auth.controller;

import com.spendsense.auth.dto.LoginRequest;
import com.spendsense.auth.dto.LoginResponse;
import com.spendsense.auth.service.AuthService;

import com.spendsense.exception.GlobalExceptionHandler;
import com.spendsense.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;


    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginResponse response = new LoginResponse(
                UUID.randomUUID(),
                "Swathi",
                "swa@gmail.com",
                "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJzd0BnbWFpbC5jb20iLCJpYXQiOjE3ODU0MjcwNDgsImV4cCI6MTc4NTQzMDY0OH0.QYO-7Y62T-U1kC-BKWRZooevArahE2H4HHc6dFs5DLn79-hx2hrCqUTxpldVe2UT"
                );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email":"swa@gmail.com",
                                    "password":"MyPassword123"
                                }
                        """)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Swathi"))
                .andExpect(jsonPath("$.email").value("swa@gmail.com"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email":"swathi@gmail.com",
                                    "password":"WrongPassword"
                                }
                        """)
        )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.messages[0]").value("Invalid email or password"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidLoginRequest() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email":"swa@gmail.com",
                                    "password":""
                                }
                                """)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));

        verify(authService,never()).login(any(LoginRequest.class));
    }
}
