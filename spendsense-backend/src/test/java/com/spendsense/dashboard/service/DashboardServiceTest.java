package com.spendsense.dashboard.service;

import com.spendsense.dashboard.dto.DashboardSummaryResponse;
import com.spendsense.expense.repository.ExpenseRepository;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");
    }

    @Test
    void shouldReturnDashboardSummary() {

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mocked =
                     mockStatic(SecurityContextHolder.class)) {

            mocked.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(user));

            LocalDate today = LocalDate.now();

            when(expenseRepository.getTodayExpense(user, today))
                    .thenReturn(BigDecimal.valueOf(500));

            when(expenseRepository.getMonthExpense(
                    eq(user),
                    any(LocalDate.class),
                    any(LocalDate.class)))
                    .thenReturn(BigDecimal.valueOf(8000));

            when(expenseRepository.getTotalExpense(user))
                    .thenReturn(BigDecimal.valueOf(25000));

            when(expenseRepository.countByCreatedBy(user))
                    .thenReturn(40L);

            DashboardSummaryResponse response =
                    dashboardService.getSummary();

            assertEquals(
                    BigDecimal.valueOf(500),
                    response.getTodayExpense()
            );

            assertEquals(
                    BigDecimal.valueOf(8000),
                    response.getMonthExpense()
            );

            assertEquals(
                    BigDecimal.valueOf(25000),
                    response.getTotalExpense()
            );

            assertEquals(
                    40L,
                    response.getExpenseCount()
            );

            verify(expenseRepository)
                    .getTodayExpense(user, today);

            verify(expenseRepository)
                    .getMonthExpense(
                            eq(user),
                            any(LocalDate.class),
                            any(LocalDate.class)
                    );

            verify(expenseRepository)
                    .getTotalExpense(user);

            verify(expenseRepository)
                    .countByCreatedBy(user);
        }
    }
}
