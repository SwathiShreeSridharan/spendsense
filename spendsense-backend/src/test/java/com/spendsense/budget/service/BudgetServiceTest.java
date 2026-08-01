package com.spendsense.budget.service;

import com.spendsense.budget.dto.BudgetResponse;
import com.spendsense.budget.dto.CreateBudgetRequest;
import com.spendsense.budget.dto.UpdateBudgetRequest;
import com.spendsense.budget.entity.Budget;
import com.spendsense.budget.entity.BudgetType;
import com.spendsense.budget.repository.BudgetRepository;
import com.spendsense.exception.BudgetAccessDeniedException;
import com.spendsense.exception.BudgetAlreadyExistsException;
import com.spendsense.exception.BudgetNotEnabledException;
import com.spendsense.exception.BudgetNotFoundException;
import com.spendsense.exception.GroupNotFoundException;
import com.spendsense.exception.InvalidBudgetPeriodException;
import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupMember;
import com.spendsense.group.entity.GroupRole;
import com.spendsense.group.entity.GroupSettings;
import com.spendsense.group.entity.GroupType;
import com.spendsense.group.repository.GroupMemberRepository;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User currentUser;
    private Group group;
    private GroupMember ownerMembership;

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

        ownerMembership = new GroupMember();
        ownerMembership.setMemberId(UUID.randomUUID());
        ownerMembership.setGroup(group);
        ownerMembership.setUser(currentUser);
        ownerMembership.setRole(GroupRole.OWNER);

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "swathi@gmail.com",
                                null
                        )
                );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateMonthlyBudgetSuccessfully() {

        CreateBudgetRequest request =
                new CreateBudgetRequest(
                        BigDecimal.valueOf(30000),
                        BudgetType.MONTHLY,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        mockCurrentUser();

        when(groupRepository
                .findByGroupIdAndArchivedFalse(group.getGroupId()))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, currentUser))
                .thenReturn(Optional.of(ownerMembership));

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

        assertNotNull(response);
        assertNotNull(response.getBudgetId());

        assertEquals(
                BigDecimal.valueOf(30000),
                response.getBudgetAmount()
        );

        assertEquals(
                BudgetType.MONTHLY,
                response.getBudgetType()
        );

        assertEquals(
                LocalDate.of(2026, 8, 1),
                response.getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 8, 31),
                response.getEndDate()
        );

        assertEquals(
                BigDecimal.ZERO,
                response.getSpentAmount()
        );

        assertEquals(
                BigDecimal.valueOf(30000),
                response.getRemainingAmount()
        );

        assertFalse(response.isExceeded());

        ArgumentCaptor<Budget> budgetCaptor =
                ArgumentCaptor.forClass(Budget.class);

        verify(budgetRepository)
                .save(budgetCaptor.capture());

        Budget savedBudget = budgetCaptor.getValue();

        assertEquals(group, savedBudget.getGroup());
        assertEquals(currentUser, savedBudget.getCreatedBy());

        assertEquals(
                BigDecimal.valueOf(30000),
                savedBudget.getAmount()
        );

        assertEquals(
                BudgetType.MONTHLY,
                savedBudget.getBudgetType()
        );

        assertFalse(savedBudget.isArchived());
    }

    @Test
    void shouldThrowGroupNotFoundWhenCreatingBudgetForMissingGroup() {

        UUID groupId = UUID.randomUUID();

        CreateBudgetRequest request =
                createMonthlyRequest();

        mockCurrentUser();

        when(groupRepository
                .findByGroupIdAndArchivedFalse(groupId))
                .thenReturn(Optional.empty());

        assertThrows(
                GroupNotFoundException.class,
                () -> budgetService.createBudget(
                        groupId,
                        request
                )
        );

        verify(groupMemberRepository, never())
                .findByGroupAndUser(any(), any());

        verify(budgetRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowGroupNotFoundWhenUserIsNotMember() {

        CreateBudgetRequest request =
                createMonthlyRequest();

        mockCurrentUser();

        when(groupRepository
                .findByGroupIdAndArchivedFalse(group.getGroupId()))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, currentUser))
                .thenReturn(Optional.empty());

        assertThrows(
                GroupNotFoundException.class,
                () -> budgetService.createBudget(
                        group.getGroupId(),
                        request
                )
        );

        verify(budgetRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowAccessDeniedWhenMemberCreatesBudget() {

        GroupMember memberMembership =
                createMembership(GroupRole.MEMBER);

        CreateBudgetRequest request =
                createMonthlyRequest();

        mockCurrentUser();

        when(groupRepository
                .findByGroupIdAndArchivedFalse(group.getGroupId()))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, currentUser))
                .thenReturn(Optional.of(memberMembership));

        assertThrows(
                BudgetAccessDeniedException.class,
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

        verify(budgetRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowBudgetNotEnabledException() {

        group.getSettings().setBudgetEnabled(false);

        CreateBudgetRequest request =
                createMonthlyRequest();

        mockCurrentUser();

        when(groupRepository
                .findByGroupIdAndArchivedFalse(group.getGroupId()))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, currentUser))
                .thenReturn(Optional.of(ownerMembership));

        assertThrows(
                BudgetNotEnabledException.class,
                () -> budgetService.createBudget(
                        group.getGroupId(),
                        request
                )
        );

        verify(budgetRepository, never())
                .save(any());
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

        mockCurrentUserAndOwner();

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

        mockCurrentUserAndOwner();

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

        mockCurrentUserAndOwner();

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

        mockCurrentUserAndOwner();

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

        mockCurrentUserAndOwner();

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

        mockCurrentUserAndOwner();

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

        mockCurrentUser();

        when(groupRepository
                .findByGroupIdAndArchivedFalse(group.getGroupId()))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, currentUser))
                .thenReturn(Optional.of(ownerMembership));

        when(budgetRepository
                .findByGroupAndArchivedFalseOrderByStartDateDesc(group))
                .thenReturn(
                        List.of(
                                augustBudget,
                                julyBudget
                        )
                );

        List<BudgetResponse> responses =
                budgetService.getGroupBudgets(
                        group.getGroupId()
                );

        assertEquals(2, responses.size());

        assertEquals(
                LocalDate.of(2026, 8, 1),
                responses.get(0).getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 7, 1),
                responses.get(1).getStartDate()
        );
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

        mockCurrentUser();

        when(groupRepository
                .findByGroupIdAndArchivedFalse(group.getGroupId()))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, currentUser))
                .thenReturn(Optional.of(ownerMembership));

        when(budgetRepository
                .findByBudgetIdAndArchivedFalse(budget.getBudgetId()))
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
    }

    @Test
    void shouldThrowWhenBudgetDoesNotBelongToGroup() {

        Group anotherGroup = createAnotherGroup();

        Budget budget =
                createBudget(
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(30000),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        budget.setGroup(anotherGroup);

        mockCurrentUser();

        when(groupRepository
                .findByGroupIdAndArchivedFalse(group.getGroupId()))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, currentUser))
                .thenReturn(Optional.of(ownerMembership));

        when(budgetRepository
                .findByBudgetIdAndArchivedFalse(budget.getBudgetId()))
                .thenReturn(Optional.of(budget));

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetService.getBudgetById(
                        group.getGroupId(),
                        budget.getBudgetId()
                )
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

        mockCurrentUserAndOwner();

        when(budgetRepository
                .findByBudgetIdAndArchivedFalse(budget.getBudgetId()))
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

        verify(budgetRepository)
                .save(budget);
    }

    @Test
    void shouldThrowWhenMemberUpdatesBudget() {

        Budget budget =
                createBudget(
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(30000),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        GroupMember memberMembership =
                createMembership(GroupRole.MEMBER);

        UpdateBudgetRequest request =
                new UpdateBudgetRequest(
                        BigDecimal.valueOf(35000),
                        BudgetType.MONTHLY,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        mockCurrentUser();

        when(groupRepository
                .findByGroupIdAndArchivedFalse(group.getGroupId()))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, currentUser))
                .thenReturn(Optional.of(memberMembership));

        assertThrows(
                BudgetAccessDeniedException.class,
                () -> budgetService.updateBudget(
                        group.getGroupId(),
                        budget.getBudgetId(),
                        request
                )
        );

        verify(budgetRepository, never())
                .save(any());
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

        mockCurrentUserAndOwner();

        when(budgetRepository
                .findByBudgetIdAndArchivedFalse(budget.getBudgetId()))
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

        verify(budgetRepository, never())
                .save(any());
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

        mockCurrentUserAndOwner();

        when(budgetRepository
                .findByBudgetIdAndArchivedFalse(budget.getBudgetId()))
                .thenReturn(Optional.of(budget));

        budgetService.archiveBudget(
                group.getGroupId(),
                budget.getBudgetId()
        );

        assertTrue(budget.isArchived());

        verify(budgetRepository)
                .save(budget);
    }

    @Test
    void shouldThrowWhenMemberArchivesBudget() {

        Budget budget =
                createBudget(
                        BudgetType.MONTHLY,
                        BigDecimal.valueOf(30000),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        GroupMember memberMembership =
                createMembership(GroupRole.MEMBER);

        mockCurrentUser();

        when(groupRepository
                .findByGroupIdAndArchivedFalse(group.getGroupId()))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, currentUser))
                .thenReturn(Optional.of(memberMembership));

        assertThrows(
                BudgetAccessDeniedException.class,
                () -> budgetService.archiveBudget(
                        group.getGroupId(),
                        budget.getBudgetId()
                )
        );

        assertFalse(budget.isArchived());

        verify(budgetRepository, never())
                .save(any());
    }

    private void mockCurrentUser() {

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));
    }

    private void mockCurrentUserAndOwner() {

        mockCurrentUser();

        when(groupRepository
                .findByGroupIdAndArchivedFalse(group.getGroupId()))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, currentUser))
                .thenReturn(Optional.of(ownerMembership));
    }

    private CreateBudgetRequest createMonthlyRequest() {

        return new CreateBudgetRequest(
                BigDecimal.valueOf(30000),
                BudgetType.MONTHLY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );
    }

    private GroupMember createMembership(GroupRole role) {

        GroupMember membership = new GroupMember();

        membership.setMemberId(UUID.randomUUID());
        membership.setGroup(group);
        membership.setUser(currentUser);
        membership.setRole(role);

        return membership;
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

    private Group createAnotherGroup() {

        GroupSettings settings = new GroupSettings();
        settings.setBudgetEnabled(true);

        Group anotherGroup = new Group();
        anotherGroup.setGroupId(UUID.randomUUID());
        anotherGroup.setName("Another Group");
        anotherGroup.setGroupType(GroupType.CUSTOM);
        anotherGroup.setArchived(false);
        anotherGroup.setCreatedBy(currentUser);
        anotherGroup.setSettings(settings);

        return anotherGroup;
    }
}