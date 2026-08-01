package com.spendsense.group.service;

import com.spendsense.exception.DuplicateGroupException;
import com.spendsense.exception.GroupNotFoundException;
import com.spendsense.group.dto.CreateGroupRequest;
import com.spendsense.group.dto.GroupResponse;
import com.spendsense.group.dto.UpdateGroupRequest;
import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupMember;
import com.spendsense.group.entity.GroupRole;
import com.spendsense.group.entity.GroupType;
import com.spendsense.group.entity.GroupSettings;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GroupService groupService;

    private User currentUser;

    @BeforeEach
    void setUp() {

        currentUser = new User();
        currentUser.setUserId(UUID.randomUUID());
        currentUser.setName("Swathi");
        currentUser.setEmail("swathi@gmail.com");

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
    void shouldCreateFamilyGroupSuccessfully() {

        CreateGroupRequest request =
                new CreateGroupRequest(
                        "Family",
                        "Family expense management",
                        GroupType.FAMILY,
                        "#2196F3",
                        "home",
                        true,
                        true,
                        true
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .existsByCreatedByAndNameIgnoreCaseAndArchivedFalse(
                        currentUser,
                        "Family"
                ))
                .thenReturn(false);

        when(groupRepository.save(any(Group.class)))
                .thenAnswer(invocation -> {

                    Group group = invocation.getArgument(0);
                    group.setGroupId(UUID.randomUUID());

                    return group;
                });

        GroupResponse response =
                groupService.createGroup(request);

        assertNotNull(response);
        assertNotNull(response.getGroupId());

        assertEquals(
                "Family",
                response.getName()
        );

        assertEquals(
                GroupType.FAMILY,
                response.getGroupType()
        );

        assertTrue(response.isBudgetEnabled());
        assertTrue(response.isNotificationEnabled());

        assertFalse(response.isSplitEnabled());

        assertEquals(
                currentUser.getUserId(),
                response.getCreatedById()
        );

        assertEquals(
                "Swathi",
                response.getCreatedByName()
        );

        ArgumentCaptor<Group> groupCaptor =
                ArgumentCaptor.forClass(Group.class);

        verify(groupRepository)
                .save(groupCaptor.capture());

        Group savedGroup = groupCaptor.getValue();

        assertEquals(
                "Family",
                savedGroup.getName()
        );

        assertEquals(
                currentUser,
                savedGroup.getCreatedBy()
        );

        assertNotNull(savedGroup.getSettings());

        assertFalse(
                savedGroup
                        .getSettings()
                        .isSplitEnabled()
        );

        ArgumentCaptor<GroupMember> memberCaptor =
                ArgumentCaptor.forClass(GroupMember.class);

        verify(groupMemberRepository)
                .save(memberCaptor.capture());

        GroupMember ownerMembership =
                memberCaptor.getValue();

        assertEquals(
                savedGroup,
                ownerMembership.getGroup()
        );

        assertEquals(
                currentUser,
                ownerMembership.getUser()
        );

        assertEquals(
                GroupRole.OWNER,
                ownerMembership.getRole()
        );
    }

    @Test
    void shouldCreateCustomGroupWithSplitEnabled() {

        CreateGroupRequest request =
                new CreateGroupRequest(
                        "Goa Trip",
                        "Goa trip expenses",
                        GroupType.CUSTOM,
                        null,
                        null,
                        true,
                        true,
                        true
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .existsByCreatedByAndNameIgnoreCaseAndArchivedFalse(
                        currentUser,
                        "Goa Trip"
                ))
                .thenReturn(false);

        when(groupRepository.save(any(Group.class)))
                .thenAnswer(invocation -> {

                    Group group = invocation.getArgument(0);
                    group.setGroupId(UUID.randomUUID());

                    return group;
                });

        GroupResponse response =
                groupService.createGroup(request);

        assertEquals(
                GroupType.CUSTOM,
                response.getGroupType()
        );

        assertTrue(response.isSplitEnabled());

        /*
         * Default values should be applied when
         * color and icon are missing.
         */
        assertEquals(
                "#2196F3",
                response.getColor()
        );

        assertEquals(
                "group",
                response.getIcon()
        );

        verify(groupRepository)
                .save(any(Group.class));

        verify(groupMemberRepository)
                .save(any(GroupMember.class));
    }

    @Test
    void shouldThrowDuplicateGroupExceptionWhenNameAlreadyExists() {

        CreateGroupRequest request =
                new CreateGroupRequest(
                        "Family",
                        "Family expense management",
                        GroupType.FAMILY,
                        "#2196F3",
                        "home",
                        true,
                        false,
                        true
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .existsByCreatedByAndNameIgnoreCaseAndArchivedFalse(
                        currentUser,
                        "Family"
                ))
                .thenReturn(true);

        DuplicateGroupException exception =
                assertThrows(
                        DuplicateGroupException.class,
                        () -> groupService.createGroup(request)
                );

        assertEquals(
                "Group already exists",
                exception.getMessage()
        );

        verify(groupRepository, never())
                .save(any(Group.class));

        verify(groupMemberRepository, never())
                .save(any(GroupMember.class));
    }

    @Test
    void shouldReturnAllGroupsCurrentUserBelongsTo() {

        Group personalGroup =
                createGroup(
                        "Personal",
                        GroupType.PERSONAL,
                        false
                );

        Group familyGroup =
                createGroup(
                        "Family",
                        GroupType.FAMILY,
                        false
                );

        GroupMember personalMembership =
                createMembership(
                        personalGroup,
                        currentUser,
                        GroupRole.OWNER
                );

        GroupMember familyMembership =
                createMembership(
                        familyGroup,
                        currentUser,
                        GroupRole.MEMBER
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupMemberRepository.findByUser(currentUser))
                .thenReturn(
                        List.of(
                                personalMembership,
                                familyMembership
                        )
                );

        List<GroupResponse> responses =
                groupService.getMyGroups();

        assertEquals(
                2,
                responses.size()
        );

        assertEquals(
                "Personal",
                responses.get(0).getName()
        );

        assertEquals(
                "Family",
                responses.get(1).getName()
        );

        verify(groupMemberRepository)
                .findByUser(currentUser);
    }

    @Test
    void shouldReturnGroupByIdWhenCurrentUserIsMember() {

        Group group =
                createGroup(
                        "Family",
                        GroupType.FAMILY,
                        false
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .findByGroupIdAndArchivedFalse(
                        group.getGroupId()
                ))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .existsByGroupAndUser(
                        group,
                        currentUser
                ))
                .thenReturn(true);

        GroupResponse response =
                groupService.getGroupById(
                        group.getGroupId()
                );

        assertNotNull(response);

        assertEquals(
                group.getGroupId(),
                response.getGroupId()
        );

        assertEquals(
                "Family",
                response.getName()
        );

        verify(groupMemberRepository)
                .existsByGroupAndUser(
                        group,
                        currentUser
                );
    }

    @Test
    void shouldThrowGroupNotFoundExceptionWhenGroupDoesNotExist() {

        UUID groupId = UUID.randomUUID();

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .findByGroupIdAndArchivedFalse(groupId))
                .thenReturn(Optional.empty());

        assertThrows(
                GroupNotFoundException.class,
                () -> groupService.getGroupById(groupId)
        );

        verify(groupMemberRepository, never())
                .existsByGroupAndUser(
                        any(Group.class),
                        any(User.class)
                );
    }

    @Test
    void shouldThrowGroupNotFoundExceptionWhenCurrentUserIsNotMember() {

        Group group =
                createGroup(
                        "Private Group",
                        GroupType.CUSTOM,
                        false
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .findByGroupIdAndArchivedFalse(
                        group.getGroupId()
                ))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .existsByGroupAndUser(
                        group,
                        currentUser
                ))
                .thenReturn(false);

        assertThrows(
                GroupNotFoundException.class,
                () -> groupService.getGroupById(
                        group.getGroupId()
                )
        );
    }

    @Test
    void shouldUpdateGroupSuccessfullyWhenCurrentUserIsOwner() {

        Group group =
                createGroup(
                        "Family",
                        GroupType.FAMILY,
                        false
                );

        GroupMember ownerMembership =
                createMembership(
                        group,
                        currentUser,
                        GroupRole.OWNER
                );

        UpdateGroupRequest request =
                new UpdateGroupRequest(
                        "My Family",
                        "Updated family description",
                        "#4CAF50",
                        "family"
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .findByGroupIdAndArchivedFalse(
                        group.getGroupId()
                ))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(
                        group,
                        currentUser
                ))
                .thenReturn(Optional.of(ownerMembership));

        when(groupRepository
                .existsByCreatedByAndNameIgnoreCaseAndArchivedFalse(
                        currentUser,
                        "My Family"
                ))
                .thenReturn(false);

        when(groupRepository.save(group))
                .thenReturn(group);

        GroupResponse response =
                groupService.updateGroup(
                        group.getGroupId(),
                        request
                );

        assertEquals(
                "My Family",
                response.getName()
        );

        assertEquals(
                "Updated family description",
                response.getDescription()
        );

        assertEquals(
                "#4CAF50",
                response.getColor()
        );

        assertEquals(
                "family",
                response.getIcon()
        );

        verify(groupRepository)
                .save(group);
    }

    @Test
    void shouldNotCheckDuplicateNameWhenNameHasNotChanged() {

        Group group =
                createGroup(
                        "Family",
                        GroupType.FAMILY,
                        false
                );

        GroupMember ownerMembership =
                createMembership(
                        group,
                        currentUser,
                        GroupRole.OWNER
                );

        UpdateGroupRequest request =
                new UpdateGroupRequest(
                        "family",
                        "New description",
                        "#4CAF50",
                        "home"
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .findByGroupIdAndArchivedFalse(
                        group.getGroupId()
                ))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(
                        group,
                        currentUser
                ))
                .thenReturn(Optional.of(ownerMembership));

        when(groupRepository.save(group))
                .thenReturn(group);

        GroupResponse response =
                groupService.updateGroup(
                        group.getGroupId(),
                        request
                );

        assertEquals(
                "family",
                response.getName()
        );

        verify(
                groupRepository,
                never()
        ).existsByCreatedByAndNameIgnoreCaseAndArchivedFalse(
                any(User.class),
                anyString()
        );

        verify(groupRepository)
                .save(group);
    }

    @Test
    void shouldThrowDuplicateGroupExceptionWhenUpdatedNameAlreadyExists() {

        Group group =
                createGroup(
                        "Family",
                        GroupType.FAMILY,
                        false
                );

        GroupMember ownerMembership =
                createMembership(
                        group,
                        currentUser,
                        GroupRole.OWNER
                );

        UpdateGroupRequest request =
                new UpdateGroupRequest(
                        "Goa Trip",
                        "Updated description",
                        "#4CAF50",
                        "trip"
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .findByGroupIdAndArchivedFalse(
                        group.getGroupId()
                ))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(
                        group,
                        currentUser
                ))
                .thenReturn(Optional.of(ownerMembership));

        when(groupRepository
                .existsByCreatedByAndNameIgnoreCaseAndArchivedFalse(
                        currentUser,
                        "Goa Trip"
                ))
                .thenReturn(true);

        assertThrows(
                DuplicateGroupException.class,
                () -> groupService.updateGroup(
                        group.getGroupId(),
                        request
                )
        );

        verify(groupRepository, never())
                .save(group);
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerUpdatesGroup() {

        Group group =
                createGroup(
                        "Family",
                        GroupType.FAMILY,
                        false
                );

        GroupMember memberMembership =
                createMembership(
                        group,
                        currentUser,
                        GroupRole.MEMBER
                );

        UpdateGroupRequest request =
                new UpdateGroupRequest(
                        "Updated Family",
                        "Updated description",
                        "#4CAF50",
                        "home"
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .findByGroupIdAndArchivedFalse(
                        group.getGroupId()
                ))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(
                        group,
                        currentUser
                ))
                .thenReturn(Optional.of(memberMembership));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> groupService.updateGroup(
                                group.getGroupId(),
                                request
                        )
                );

        assertEquals(
                "Only the group owner can update the group",
                exception.getMessage()
        );

        verify(groupRepository, never())
                .save(group);
    }

    @Test
    void shouldArchiveGroupSuccessfullyWhenCurrentUserIsOwner() {

        Group group =
                createGroup(
                        "Goa Trip",
                        GroupType.CUSTOM,
                        false
                );

        GroupMember ownerMembership =
                createMembership(
                        group,
                        currentUser,
                        GroupRole.OWNER
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .findByGroupIdAndArchivedFalse(
                        group.getGroupId()
                ))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(
                        group,
                        currentUser
                ))
                .thenReturn(Optional.of(ownerMembership));

        groupService.archiveGroup(
                group.getGroupId()
        );

        assertTrue(group.isArchived());

        verify(groupRepository)
                .save(group);
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerArchivesGroup() {

        Group group =
                createGroup(
                        "Goa Trip",
                        GroupType.CUSTOM,
                        false
                );

        GroupMember memberMembership =
                createMembership(
                        group,
                        currentUser,
                        GroupRole.MEMBER
                );

        when(userRepository.findByEmail("swathi@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        when(groupRepository
                .findByGroupIdAndArchivedFalse(
                        group.getGroupId()
                ))
                .thenReturn(Optional.of(group));

        when(groupMemberRepository
                .findByGroupAndUser(
                        group,
                        currentUser
                ))
                .thenReturn(Optional.of(memberMembership));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> groupService.archiveGroup(
                                group.getGroupId()
                        )
                );

        assertEquals(
                "Only the group owner can archive the group",
                exception.getMessage()
        );

        assertFalse(group.isArchived());

        verify(groupRepository, never())
                .save(group);
    }

    private Group createGroup(
            String name,
            GroupType groupType,
            boolean archived
    ) {

        Group group = new Group();

        group.setGroupId(UUID.randomUUID());
        group.setName(name);
        group.setDescription(name + " description");
        group.setGroupType(groupType);
        group.setColor("#2196F3");
        group.setIcon("group");
        group.setArchived(archived);
        group.setCreatedBy(currentUser);

        GroupSettings settings =
                new GroupSettings();

        settings.setBudgetEnabled(true);
        settings.setSplitEnabled(
                groupType == GroupType.CUSTOM
        );
        settings.setNotificationEnabled(true);

        group.setSettings(settings);

        return group;
    }

    private GroupMember createMembership(
            Group group,
            User user,
            GroupRole role
    ) {

        GroupMember groupMember =
                new GroupMember();

        groupMember.setMemberId(UUID.randomUUID());
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setRole(role);

        return groupMember;
    }
}
