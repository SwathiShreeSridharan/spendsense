package com.spendsense.user.controller;



import com.spendsense.user.exception.DuplicateEmailException;
import com.spendsense.user.dto.RegisterUserRequest;
import com.spendsense.user.dto.UserResponse;
import com.spendsense.user.entity.UserStatus;
import com.spendsense.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(),
                "Swathi",
                "swa@gmail.com",
                "1234567890",
                UserStatus.ACTIVE,
                LocalDateTime.now()
        );

        when(userService.registerUser(any(RegisterUserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name":"Swathi",
                                    "email":"swa@gmail.com",
                                    "mobileNumber":"1234567890",
                                    "password":"MyPassword123"
                                    }
                                """)
        )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Swathi"))
        .andExpect(jsonPath("$.email").value("swa@gmail.com"))
        .andExpect(jsonPath("$.mobileNumber").value("1234567890"))
        .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldReturnBadRequestForInvalidUserRequest() throws Exception {
        mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {
                                    "name":"",
                                    "email":"swathi",
                                    "mobileNumber":"",
                                    "password":"123"
                                 }
                        """)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.messages").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/users"));

        verify(userService, never()).registerUser(any(RegisterUserRequest.class));
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        when(userService.registerUser(any(RegisterUserRequest.class)))
                .thenThrow(
                        new DuplicateEmailException("Email is already registered")
                );

        mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name":"Swathi",
                                    "email":"swa@gmail.com",
                                    "mobileNumber":"1234567890",
                                    "password":"MyPassword123"
                                }
                                """)
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.messages[0]").value("Email is already registered"))
                .andExpect(jsonPath("$.path").value("/api/v1/users"));

        verify(userService)
                .registerUser(any(RegisterUserRequest.class));
    }
}
