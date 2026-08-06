package com.spendsense.group.service;

import com.spendsense.exception.GroupAccessDeniedException;
import com.spendsense.exception.GroupNotFoundException;
import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupMember;
import com.spendsense.group.entity.GroupRole;
import com.spendsense.group.repository.GroupMemberRepository;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupAccessServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private GroupAccessService groupAccessService;

    private UUID groupId;
    private User user;
    private Group group;
    private GroupMember membership;

    @BeforeEach
    void setUp() {
        groupId = UUID.randomUUID();

        user = new User();
        user.setUserId(UUID.randomUUID());

        group = new Group();
        group.setGroupId(groupId);
        group.setName("Family");

        membership = new GroupMember();
        membership.setGroup(group);
        membership.setUser(user);
        membership.setRole(GroupRole.MEMBER);
    }

    @Test
    void shouldReturnGroupWhenUserIsMember() {

        when(groupRepository
                .findByGroupIdAndArchivedFalse(groupId))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, user))
                .thenReturn(Optional.of(membership));

        Group result =
                groupAccessService.requireMember(
                        groupId,
                        user
                );

        assertEquals(group, result);
    }

    @Test
    void shouldRejectWhenGroupDoesNotExist() {

        when(groupRepository
                .findByGroupIdAndArchivedFalse(groupId))
                .thenReturn(Optional.empty());

        assertThrows(
                GroupNotFoundException.class,
                () -> groupAccessService.requireMember(
                        groupId,
                        user
                )
        );
    }

    @Test
    void shouldRejectWhenUserIsNotMember() {

        when(groupRepository
                .findByGroupIdAndArchivedFalse(groupId))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, user))
                .thenReturn(Optional.empty());

        assertThrows(
                GroupNotFoundException.class,
                () -> groupAccessService.requireMember(
                        groupId,
                        user
                )
        );
    }

    @Test
    void shouldAllowOwnerForOwnerOperation() {

        mockMembership(GroupRole.OWNER);

        Group result =
                groupAccessService.requireOwner(
                        groupId,
                        user
                );

        assertEquals(group, result);
    }

    @Test
    void shouldRejectMemberForOwnerOperation() {

        mockMembership(GroupRole.MEMBER);

        assertThrows(
                GroupAccessDeniedException.class,
                () -> groupAccessService.requireOwner(
                        groupId,
                        user
                )
        );
    }

    @Test
    void shouldAllowAdminForAdminOperation() {

        mockMembership(GroupRole.ADMIN);

        Group result =
                groupAccessService.requireOwnerOrAdmin(
                        groupId,
                        user
                );

        assertEquals(group, result);
    }

    private void mockMembership(GroupRole role) {

        membership.setRole(role);

        when(groupRepository
                .findByGroupIdAndArchivedFalse(groupId))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(group, user))
                .thenReturn(Optional.of(membership));
    }
}
