package com.spendsense.budget.service;

import com.spendsense.budget.dto.BudgetResponse;
import com.spendsense.budget.dto.CreateBudgetRequest;
import com.spendsense.budget.dto.UpdateBudgetRequest;
import com.spendsense.budget.entity.Budget;
import com.spendsense.budget.entity.BudgetType;
import com.spendsense.budget.repository.BudgetRepository;
import com.spendsense.exception.*;
import com.spendsense.expense.repository.ExpenseRepository;
import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupMember;
import com.spendsense.group.entity.GroupRole;
import com.spendsense.group.entity.GroupSettings;
import com.spendsense.group.entity.GroupType;
import com.spendsense.group.repository.GroupMemberRepository;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.group.service.GroupAccessService;
import com.spendsense.security.CurrentUserService;
import com.spendsense.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GroupAccessService groupAccessService;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User currentUser;
    private Group group;

    @BeforeEach
    void setUp() {

        currentUser = new User();
        currentUser.setUserId(UUID.randomUUID());
        currentUser.setName("Swathi");
        currentUser.setEmail("swathi@gmail.com");

        GroupSettings settings = new GroupSettings();
        settings.setBudgetEnabled(true);
        settings.setSplitEnabled(false);
        settings.setNotificationEnabled(true);

        group = new Group();
        group.setGroupId(UUID.randomUUID());
        group.setName("Family");
        group.setDescription("Family expenses");
        group.setGroupType(GroupType.FAMILY);
        group.setColor("#2196F3");
        group.setIcon("home");
        group.setArchived(false);
        group.setCreatedBy(currentUser);
        group.setSettings(settings);

    }


    @Test
    void shouldCreateMonthlyBudgetSuccessfully() {
        mockCurrentUserAndBudgetManager();

        CreateBudgetRequest request = new CreateBudgetRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setBudgetType(BudgetType.MONTHLY);
        request.setStartDate(LocalDate.of(2026, 8, 1));
        request.setEndDate(LocalDate.of(2026, 8, 31));

        when(budgetRepository.existsOverlappingBudget(
                group,
                request.getStartDate(),
                request.getEndDate()
        )).thenReturn(false);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BudgetResponse response = budgetService.createBudget(
                group.getGroupId(),
                request
        );

        assertNotNull(response);
        assertEquals(request.getAmount(), response.getBudgetAmount());
        assertEquals(request.getBudgetType(), response.getBudgetType());
        assertEquals(request.getStartDate(), response.getStartDate());
        assertEquals(request.getEndDate(), response.getEndDate());

        verify(currentUserService).getCurrentUser();

        verify(groupAccessService).requireOwnerOrAdmin(
                group.getGroupId(),
                currentUser
        );

        verify(budgetRepository).existsOverlappingBudget(
                group,
                request.getStartDate(),
                request.getEndDate()
        );

        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void shouldThrowGroupNotFoundWhenCreatingBudgetForMissingGroup() {
        mockCurrentUser();

        CreateBudgetRequest request = new CreateBudgetRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setBudgetType(BudgetType.MONTHLY);
        request.setStartDate(LocalDate.of(2026, 8, 1));
        request.setEndDate(LocalDate.of(2026, 8, 31));

        when(groupAccessService.requireOwnerOrAdmin(
                group.getGroupId(),
                currentUser
        )).thenThrow(new GroupNotFoundException("Group not found"));

        assertThrows(
                GroupNotFoundException.class,
                () -> budgetService.createBudget(group.getGroupId(), request)
        );

        verify(budgetRepository, never()).save(any(Budget.class));
    }


    @Test
    void shouldThrowAccessDeniedWhenMemberCreatesBudget() {
        mockCurrentUser();

        CreateBudgetRequest request = new CreateBudgetRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setBudgetType(BudgetType.MONTHLY);
        request.setStartDate(LocalDate.of(2026, 8, 1));
        request.setEndDate(LocalDate.of(2026, 8, 31));

        when(groupAccessService.requireOwnerOrAdmin(
                group.getGroupId(),
                currentUser
        )).thenThrow(
                new GroupAccessDeniedException(
                        "Only the group owner or an admin can perform this action"
                )
        );

        assertThrows(
                GroupAccessDeniedException.class,
                () -> budgetService.createBudget(group.getGroupId(), request)
        );

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void shouldThrowBudgetNotEnabledException() {
        group.getSettings().setBudgetEnabled(false);

        CreateBudgetRequest request = createMonthlyRequest();

        mockCurrentUserAndBudgetManager();

        assertThrows(
                BudgetNotEnabledException.class,
                () -> budgetService.createBudget(
                        group.getGroupId(),
                        request
                )
        );

        verify(groupAccessService).requireOwnerOrAdmin(
                group.getGroupId(),
                currentUser
        );

        verify(budgetRepository, never())
                .existsOverlappingBudget(
                        any(),
                        any(),
                        any()
                );

        verify(budgetRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenStartDateIsAfterEndDate() {

        CreateBudgetRequest request =
                new CreateBudgetRequest(
                        BigDecimal.valueOf(30000),
                        BudgetType.CUSTOM,
                        LocalDate.of(2026, 8, 31),
                        LocalDate.of(2026, 8, 1)
                );

        mockCurrentUserAndBudgetManager();

        assertThrows(
                InvalidBudgetPeriodException.class,
                () -> budgetService.createBudget(
                        group.getGroupId(),
                        request
                )
        );

        verify(budgetRepository, never())
                .existsOverlappingBudget(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldThrowWhenMonthlyPeriodIsIncomplete() {

        CreateBudgetRequest request =
                new CreateBudgetRequest(
                        BigDecimal.valueOf(30000),
                        BudgetType.MONTHLY,
                        LocalDate.of(2026, 8, 5),
                        LocalDate.of(2026, 8, 31)
                );

        mockCurrentUserAndBudgetManager();

        assertThrows(
                InvalidBudgetPeriodException.class,
                () -> budgetService.createBudget(
                        group.getGroupId(),
                        request
                )
        );
    }

    @Test
    void shouldThrowWhenQuarterlyPeriodIsInvalid() {

        CreateBudgetRequest request =
                new CreateBudgetRequest(
                        BigDecimal.valueOf(90000),
                        BudgetType.QUARTERLY,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 4, 30)
                );

        mockCurrentUserAndBudgetManager();

        assertThrows(
                InvalidBudgetPeriodException.class,
                () -> budgetService.createBudget(
                        group.getGroupId(),
                        request
                )
        );
    }

    @Test
    void shouldCreateValidQuarterlyBudget() {

        CreateBudgetRequest request =
                new CreateBudgetRequest(
                        BigDecimal.valueOf(90000),
                        BudgetType.QUARTERLY,
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 9, 30)
                );

        mockCurrentUserAndBudgetManager();

        when(budgetRepository.existsOverlappingBudget(
                group,
                request.getStartDate(),
                request.getEndDate()
        )).thenReturn(false);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> {
                    Budget budget = invocation.getArgument(0);
                    budget.setBudgetId(UUID.randomUUID());
                    return budget;
                });

        BudgetResponse response =
                budgetService.createBudget(
                        group.getGroupId(),
                        request
                );

        assertEquals(
                BudgetType.QUARTERLY,
                response.getBudgetType()
        );

        assertEquals(
                LocalDate.of(2026, 7, 1),
                response.getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 9, 30),
                response.getEndDate()
        );
    }

    @Test
    void shouldThrowWhenYearlyPeriodIsInvalid() {

        CreateBudgetRequest request =
                new CreateBudgetRequest(
                        BigDecimal.valueOf(300000),
                        BudgetType.YEARLY,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 12, 31)
                );

        mockCurrentUserAndBudgetManager();

        assertThrows(
                InvalidBudgetPeriodException.class,
                () -> budgetService.createBudget(
                        group.getGroupId(),
                        request
                )
        );
    }

    @Test
    void shouldThrowWhenOverlappingBudgetExists() {

        CreateBudgetRequest request =
                createMonthlyRequest();

        mockCurrentUserAndBudgetManager();

        when(budgetRepository.existsOverlappingBudget(
                group,
                request.getStartDate(),
                request.getEndDate()
        )).thenReturn(true);

        assertThrows(
                BudgetAlreadyExistsException.class,
                () -> budgetService.createBudget(
                        group.getGroupId(),
                        request
                )
        );

        verify(budgetRepository, never())
                .save(any());
    }

    @Test
    void shouldReturnAllGroupBudgets() {
        Budget augustBudget =
                createBudget(
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(30000),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        Budget julyBudget =
                createBudget(
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(25000),
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 31)
                );

        mockCurrentUserAndGroupMember();

        when(budgetRepository
                .findByGroupAndArchivedFalseOrderByStartDateDesc(group))
                .thenReturn(List.of(augustBudget, julyBudget));

        List<BudgetResponse> responses =
                budgetService.getGroupBudgets(group.getGroupId());

        assertEquals(2, responses.size());

        assertEquals(
                LocalDate.of(2026, 8, 1),
                responses.get(0).getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 7, 1),
                responses.get(1).getStartDate()
        );

        verify(groupAccessService).requireMember(
                group.getGroupId(),
                currentUser
        );

        verify(budgetRepository)
                .findByGroupAndArchivedFalseOrderByStartDateDesc(group);
    }

    @Test
    void shouldReturnBudgetById() {
        Budget budget =
                createBudget(
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(30000),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        mockCurrentUserAndGroupMember();

        when(budgetRepository
                .findByBudgetIdAndGroupAndArchivedFalse(
                        budget.getBudgetId(),
                        group
                ))
                .thenReturn(Optional.of(budget));

        BudgetResponse response =
                budgetService.getBudgetById(
                        group.getGroupId(),
                        budget.getBudgetId()
                );

        assertEquals(
                budget.getBudgetId(),
                response.getBudgetId()
        );

        assertEquals(
                group.getGroupId(),
                response.getGroupId()
        );

        verify(groupAccessService).requireMember(
                group.getGroupId(),
                currentUser
        );
    }

    @Test
    void shouldThrowWhenBudgetDoesNotBelongToGroup() {
        UUID budgetId = UUID.randomUUID();

        mockCurrentUserAndGroupMember();

        when(budgetRepository
                .findByBudgetIdAndGroupAndArchivedFalse(
                        budgetId,
                        group
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetService.getBudgetById(
                        group.getGroupId(),
                        budgetId
                )
        );

        verify(groupAccessService).requireMember(
                group.getGroupId(),
                currentUser
        );
    }

    @Test
    void shouldUpdateBudgetSuccessfully() {
        Budget budget =
                createBudget(
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(30000),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        UpdateBudgetRequest request =
                new UpdateBudgetRequest(
                        BigDecimal.valueOf(35000),
                        BudgetType.MONTHLY,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        mockCurrentUserAndBudgetManager();

        when(budgetRepository
                .findByBudgetIdAndGroupAndArchivedFalse(
                        budget.getBudgetId(),
                        group
                ))
                .thenReturn(Optional.of(budget));

        when(budgetRepository
                .existsOverlappingBudgetExcludingCurrent(
                        group,
                        budget.getBudgetId(),
                        request.getStartDate(),
                        request.getEndDate()
                ))
                .thenReturn(false);

        when(budgetRepository.save(budget))
                .thenReturn(budget);

        BudgetResponse response =
                budgetService.updateBudget(
                        group.getGroupId(),
                        budget.getBudgetId(),
                        request
                );

        assertEquals(
                BigDecimal.valueOf(35000),
                response.getBudgetAmount()
        );

        assertEquals(
                request.getBudgetType(),
                response.getBudgetType()
        );

        verify(groupAccessService).requireOwnerOrAdmin(
                group.getGroupId(),
                currentUser
        );

        verify(budgetRepository).save(budget);
    }

    @Test
    void shouldThrowWhenMemberUpdatesBudget() {
        UUID budgetId = UUID.randomUUID();

        UpdateBudgetRequest request =
                new UpdateBudgetRequest(
                        BigDecimal.valueOf(35000),
                        BudgetType.MONTHLY,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        mockCurrentUser();

        when(groupAccessService.requireOwnerOrAdmin(
                group.getGroupId(),
                currentUser
        )).thenThrow(
                new GroupAccessDeniedException(
                        "Only the group owner or an admin can perform this action"
                )
        );

        assertThrows(
                GroupAccessDeniedException.class,
                () -> budgetService.updateBudget(
                        group.getGroupId(),
                        budgetId,
                        request
                )
        );

        verify(budgetRepository, never())
                .findByBudgetIdAndGroupAndArchivedFalse(
                        any(),
                        any()
                );

        verify(budgetRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUpdatedBudgetOverlapsAnotherBudget() {
        Budget budget =
                createBudget(
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(30000),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        UpdateBudgetRequest request =
                new UpdateBudgetRequest(
                        BigDecimal.valueOf(35000),
                        BudgetType.CUSTOM,
                        LocalDate.of(2026, 8, 15),
                        LocalDate.of(2026, 9, 15)
                );

        mockCurrentUserAndBudgetManager();

        when(budgetRepository
                .findByBudgetIdAndGroupAndArchivedFalse(
                        budget.getBudgetId(),
                        group
                ))
                .thenReturn(Optional.of(budget));

        when(budgetRepository
                .existsOverlappingBudgetExcludingCurrent(
                        group,
                        budget.getBudgetId(),
                        request.getStartDate(),
                        request.getEndDate()
                ))
                .thenReturn(true);

        assertThrows(
                BudgetAlreadyExistsException.class,
                () -> budgetService.updateBudget(
                        group.getGroupId(),
                        budget.getBudgetId(),
                        request
                )
        );

        verify(budgetRepository, never()).save(any());
    }

    @Test
    void shouldArchiveBudgetSuccessfully() {
        Budget budget =
                createBudget(
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(30000),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        mockCurrentUserAndBudgetManager();

        when(budgetRepository
                .findByBudgetIdAndGroupAndArchivedFalse(
                        budget.getBudgetId(),
                        group
                ))
                .thenReturn(Optional.of(budget));

        budgetService.archiveBudget(
                group.getGroupId(),
                budget.getBudgetId()
        );

        assertTrue(budget.isArchived());

        verify(groupAccessService).requireOwnerOrAdmin(
                group.getGroupId(),
                currentUser
        );

        verify(budgetRepository).save(budget);
    }

    @Test
    void shouldThrowWhenMemberArchivesBudget() {
        UUID budgetId = UUID.randomUUID();

        mockCurrentUser();

        when(groupAccessService.requireOwnerOrAdmin(
                group.getGroupId(),
                currentUser
        )).thenThrow(
                new GroupAccessDeniedException(
                        "Only the group owner or an admin can perform this action"
                )
        );

        assertThrows(
                GroupAccessDeniedException.class,
                () -> budgetService.archiveBudget(
                        group.getGroupId(),
                        budgetId
                )
        );

        verify(budgetRepository, never())
                .findByBudgetIdAndGroupAndArchivedFalse(
                        any(),
                        any()
                );

        verify(budgetRepository, never()).save(any());
    }

    private void mockCurrentUser() {

        when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);
    }



    private void mockCurrentUserAndBudgetManager() {
        mockCurrentUser();

        when(groupAccessService.requireOwnerOrAdmin(
                group.getGroupId(),
                currentUser
        )).thenReturn(group);
    }

    private void mockCurrentUserAndGroupMember() {
        mockCurrentUser();

        when(groupAccessService.requireMember(
                group.getGroupId(),
                currentUser
        )).thenReturn(group);
    }

    private CreateBudgetRequest createMonthlyRequest() {

        return new CreateBudgetRequest(
                BigDecimal.valueOf(30000),
                BudgetType.MONTHLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );
    }


    private Budget createBudget(
            BudgetType budgetType,
            BigDecimal amount,
            LocalDate startDate,
            LocalDate endDate
    ) {

        Budget budget = new Budget(
                group,
                amount,
                budgetType,
                startDate,
                endDate,
                currentUser
        );

        budget.setBudgetId(UUID.randomUUID());

        return budget;
    }

    @Test
    void shouldCalculateBudgetUsageFromExpenses() {
        Budget budget = createBudget(
                BudgetType.MONTHLY,
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );

        mockCurrentUserAndGroupMember();

        when(budgetRepository
                .findByBudgetIdAndGroupAndArchivedFalse(
                        budget.getBudgetId(),
                        group
                ))
                .thenReturn(Optional.of(budget));

        when(expenseRepository.getTotalExpenseForBudgetPeriod(
                group,
                budget.getStartDate(),
                budget.getEndDate()
        )).thenReturn(new BigDecimal("333.33"));

        BudgetResponse response = budgetService.getBudgetById(
                group.getGroupId(),
                budget.getBudgetId()
        );

        assertEquals(
                new BigDecimal("333.33"),
                response.getSpentAmount()
        );

        assertEquals(
                new BigDecimal("666.67"),
                response.getRemainingAmount()
        );

        assertEquals(
                new BigDecimal("33.33"),
                response.getPercentageUsed()
        );

        assertFalse(response.isExceeded());

        verify(expenseRepository)
                .getTotalExpenseForBudgetPeriod(
                        group,
                        budget.getStartDate(),
                        budget.getEndDate()
                );
    }

    @Test
    void shouldMarkBudgetAsExceededWhenSpendingExceedsLimit() {
        Budget budget = createBudget(
                BudgetType.MONTHLY,
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );

        mockCurrentUserAndGroupMember();

        when(budgetRepository
                .findByBudgetIdAndGroupAndArchivedFalse(
                        budget.getBudgetId(),
                        group
                ))
                .thenReturn(Optional.of(budget));

        when(expenseRepository.getTotalExpenseForBudgetPeriod(
                group,
                budget.getStartDate(),
                budget.getEndDate()
        )).thenReturn(new BigDecimal("1250.50"));

        BudgetResponse response = budgetService.getBudgetById(
                group.getGroupId(),
                budget.getBudgetId()
        );

        assertEquals(
                new BigDecimal("1250.50"),
                response.getSpentAmount()
        );

        assertEquals(
                new BigDecimal("-250.50"),
                response.getRemainingAmount()
        );

        assertEquals(
                new BigDecimal("125.05"),
                response.getPercentageUsed()
        );

        assertTrue(response.isExceeded());
    }

    @Test
    void shouldNotMarkBudgetAsExceededAtExactLimit() {
        Budget budget = createBudget(
                BudgetType.MONTHLY,
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );

        mockCurrentUserAndGroupMember();

        when(budgetRepository
                .findByBudgetIdAndGroupAndArchivedFalse(
                        budget.getBudgetId(),
                        group
                ))
                .thenReturn(Optional.of(budget));

        when(expenseRepository.getTotalExpenseForBudgetPeriod(
                group,
                budget.getStartDate(),
                budget.getEndDate()
        )).thenReturn(new BigDecimal("1000.00"));

        BudgetResponse response = budgetService.getBudgetById(
                group.getGroupId(),
                budget.getBudgetId()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                response.getSpentAmount()
        );

        assertEquals(
                new BigDecimal("0.00"),
                response.getRemainingAmount()
        );

        assertEquals(
                new BigDecimal("100.00"),
                response.getPercentageUsed()
        );

        assertFalse(response.isExceeded());
    }

    @Test
    void shouldReturnActiveBudgetsForUser() {
        LocalDate today = LocalDate.of(2026, 8, 6);

        Budget budget = createBudget(
                BudgetType.MONTHLY,
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );

        when(budgetRepository.findActiveBudgetsForUser(
                currentUser,
                today
        )).thenReturn(List.of(budget));

        when(expenseRepository.getTotalExpenseForBudgetPeriod(
                group,
                budget.getStartDate(),
                budget.getEndDate()
        )).thenReturn(new BigDecimal("250.00"));

        List<BudgetResponse> responses =
                budgetService.getActiveBudgetsForUser(
                        currentUser,
                        today
                );

        assertEquals(1, responses.size());

        BudgetResponse response = responses.get(0);

        assertEquals(
                new BigDecimal("250.00"),
                response.getSpentAmount()
        );

        assertEquals(
                new BigDecimal("750.00"),
                response.getRemainingAmount()
        );

        assertEquals(
                new BigDecimal("25.00"),
                response.getPercentageUsed()
        );

        assertFalse(response.isExceeded());

        verify(budgetRepository)
                .findActiveBudgetsForUser(
                        currentUser,
                        today
                );
    }

}