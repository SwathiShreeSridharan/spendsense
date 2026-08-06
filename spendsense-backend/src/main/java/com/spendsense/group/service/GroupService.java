package com.spendsense.group.service;

import com.spendsense.exception.DuplicateGroupException;
import com.spendsense.group.dto.CreateGroupRequest;
import com.spendsense.group.dto.GroupResponse;
import com.spendsense.group.dto.UpdateGroupRequest;
import com.spendsense.group.entity.*;
import com.spendsense.group.repository.GroupMemberRepository;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.security.CurrentUserService;
import com.spendsense.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CurrentUserService currentUserService;
    private final GroupAccessService groupAccessService;

    public GroupService(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            CurrentUserService currentUserService,
            GroupAccessService groupAccessService
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.currentUserService = currentUserService;
        this.groupAccessService = groupAccessService;
    }

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {

        User currentUser = currentUserService.getCurrentUser();

        boolean exists = groupRepository
                .existsByCreatedByAndNameIgnoreCaseAndArchivedFalse(currentUser,
                        request.getName().trim()
                );

        if(exists){
            throw new DuplicateGroupException("Group already exists");
        }

        String icon = request.getIcon();

        if (icon == null || icon.isBlank()) {
            icon = "group";
        }

        String color = request.getColor();

        if (color == null || color.isBlank()) {
            color = "#2196F3";
        }

        Group group = new Group();

        group.setName(request.getName().trim());
        group.setDescription(request.getDescription());
        group.setGroupType(request.getGroupType());
        group.setColor(color);
        group.setIcon(icon);
        group.setArchived(false);
        group.setCreatedBy(currentUser);

        GroupSettings settings = new GroupSettings();

        settings.setBudgetEnabled(request.isBudgetEnabled());
        settings.setNotificationEnabled(request.isNotificationEnabled());

        if (request.getGroupType() == GroupType.CUSTOM) {
            settings.setSplitEnabled(request.isSplitEnabled());
        }

        else {
            settings.setSplitEnabled(false);
        }

        group.setSettings(settings);

        Group savedGroup = groupRepository.save(group);

        GroupMember owner = new GroupMember();

        owner.setGroup(savedGroup);
        owner.setUser(currentUser);
        owner.setRole(GroupRole.OWNER);

        groupMemberRepository.save(owner);

        return mapToResponse(savedGroup);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups() {

        User currentUser = currentUserService.getCurrentUser();

        List<GroupMember> memberships =
                groupMemberRepository.findByUser(currentUser);

        return memberships
                .stream()
                .map(GroupMember::getGroup)
                .filter(group -> !group.isArchived())
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(UUID groupId) {
        User currentUser =
                currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireMember(
                        groupId,
                        currentUser
                );

        return mapToResponse(group);

    }

    @Transactional
    public GroupResponse updateGroup(
            UUID groupId,
            UpdateGroupRequest request
    ) {
        User currentUser =
                currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireOwner(
                        groupId,
                        currentUser
                );

        String newName =
                request.getName().trim();

        if (!group.getName().equalsIgnoreCase(newName)) {

            boolean exists =
                    groupRepository
                            .existsByCreatedByAndNameIgnoreCaseAndArchivedFalse(
                                    currentUser,
                                    newName
                            );

            if (exists) {
                throw new DuplicateGroupException(
                        "Group already exists"
                );
            }
        }

        group.setName(newName);
        group.setDescription(
                request.getDescription()
        );
        group.setColor(
                request.getColor()
        );
        group.setIcon(
                request.getIcon()
        );

        Group updatedGroup =
                groupRepository.save(group);

        return mapToResponse(updatedGroup);
    }

    @Transactional
    public void archiveGroup(UUID groupId) {

        User currentUser =
                currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireOwner(
                        groupId,
                        currentUser
                );

        group.setArchived(true);

        groupRepository.save(group);
    }

    private GroupResponse mapToResponse(Group group) {
        return new GroupResponse(
                group.getGroupId(),
                group.getName(),
                group.getDescription(),
                group.getGroupType(),
                group.getColor(),
                group.getIcon(),
                group.isArchived(),
                group.getCreatedBy().getUserId(),
                group.getCreatedBy().getName(),
                group.getSettings().isBudgetEnabled(),
                group.getSettings().isSplitEnabled(),
                group.getSettings().isNotificationEnabled(),
                group.getCreatedAt()
        );
    }

}
