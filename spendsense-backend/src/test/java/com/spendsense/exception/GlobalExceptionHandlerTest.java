package com.spendsense.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler =
                new GlobalExceptionHandler();

        request = new MockHttpServletRequest();
        request.setRequestURI(
                "/api/v1/groups/test-group"
        );
    }

    @Test
    void shouldReturnConflictForDuplicateGroup() {
        DuplicateGroupException exception =
                new DuplicateGroupException(
                        "Group already exists"
                );

        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleConflict(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        ApiErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(409, body.getStatus());
        assertEquals("Conflict", body.getError());
        assertEquals(
                "Group already exists",
                body.getMessages().getFirst()
        );
        assertEquals(
                "/api/v1/groups/test-group",
                body.getPath()
        );
    }

    @Test
    void shouldReturnNotFoundForMissingGroup() {
        GroupNotFoundException exception =
                new GroupNotFoundException(
                        "Group not found"
                );

        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        ApiErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("Not Found", body.getError());
        assertEquals(
                "Group not found",
                body.getMessages().getFirst()
        );
        assertEquals(
                "/api/v1/groups/test-group",
                body.getPath()
        );
    }

    @Test
    void shouldReturnFormattedValidationMessages() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError amountError =
                new FieldError(
                        "createBudgetRequest",
                        "amount",
                        "must be greater than zero"
                );

        FieldError startDateError =
                new FieldError(
                        "createBudgetRequest",
                        "startDate",
                        "must not be null"
                );

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(
                        List.of(
                                amountError,
                                startDateError
                        )
                );

        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler
                        .handleValidationException(
                                exception,
                                request
                        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        ApiErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(400, body.getStatus());

        assertEquals(
                List.of(
                        "amount: must be greater than zero",
                        "startDate: must not be null"
                ),
                body.getMessages()
        );
    }

    @Test
    void shouldHideInternalDetailsForUnexpectedException() {
        RuntimeException exception =
                new RuntimeException(
                        "Sensitive database connection details"
                );

        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler
                        .handleUnexpectedException(
                                exception,
                                request
                        );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        ApiErrorResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals(
                "Internal Server Error",
                body.getError()
        );

        assertEquals(
                List.of("An unexpected error occurred"),
                body.getMessages()
        );

        assertFalse(
                body.getMessages()
                        .contains(
                                "Sensitive database connection details"
                        )
        );
    }
}