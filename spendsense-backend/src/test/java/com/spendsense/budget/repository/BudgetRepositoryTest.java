package com.spendsense.budget.repository;

import com.spendsense.budget.entity.Budget;
import com.spendsense.budget.entity.BudgetType;
import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupMember;
import com.spendsense.group.entity.GroupRole;
import com.spendsense.group.entity.GroupType;
import com.spendsense.group.repository.GroupMemberRepository;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BudgetRepositoryTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Test
    void shouldReturnOnlyActiveBudgetsForUsersGroups() {
        LocalDate today = LocalDate.of(2026, 8, 6);

        User currentUser = createUser("current");
        User otherUser = createUser("other");

        Group activeGroup =
                createGroup("Active Group", currentUser, false);

        Group otherGroup =
                createGroup("Other Group", otherUser, false);

        Group archivedGroup =
                createGroup("Archived Group", currentUser, true);

        addMember(activeGroup, currentUser);
        addMember(otherGroup, otherUser);
        addMember(archivedGroup, currentUser);

        Budget expectedBudget = createBudget(
                activeGroup,
                currentUser,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                false
        );

        createBudget(
                activeGroup,
                currentUser,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                false
        );

        createBudget(
                activeGroup,
                currentUser,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                true
        );

        createBudget(
                otherGroup,
                otherUser,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                false
        );

        createBudget(
                archivedGroup,
                currentUser,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                false
        );

        List<Budget> results =
                budgetRepository.findActiveBudgetsForUser(
                        currentUser,
                        today
                );

        assertEquals(1, results.size());

        assertEquals(
                expectedBudget.getBudgetId(),
                results.get(0).getBudgetId()
        );
    }

    private User createUser(String prefix) {
        return userRepository.save(
                new User(
                        prefix + " user",
                        prefix + "-" + UUID.randomUUID()
                                + "@example.com",
                        "9876543210",
                        "hashed-password"
                )
        );
    }

    private Group createGroup(
            String name,
            User owner,
            boolean archived
    ) {
        Group group = new Group();
        group.setName(name);
        group.setDescription("Budget repository test");
        group.setGroupType(GroupType.FAMILY);
        group.setColor("#2196F3");
        group.setIcon("home");
        group.setArchived(archived);
        group.setCreatedBy(owner);

        return groupRepository.save(group);
    }

    private void addMember(Group group, User user) {
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setRole(GroupRole.OWNER);

        groupMemberRepository.save(member);
    }

    private Budget createBudget(
            Group group,
            User creator,
            LocalDate startDate,
            LocalDate endDate,
            boolean archived
    ) {
        Budget budget = new Budget(
                group,
                new BigDecimal("10000.00"),
                BudgetType.MONTHLY,
                startDate,
                endDate,
                creator
        );

        budget.setArchived(archived);

        return budgetRepository.save(budget);
    }
}