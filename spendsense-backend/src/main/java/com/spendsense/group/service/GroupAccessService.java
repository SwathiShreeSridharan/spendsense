package com.spendsense.group.service;

import com.spendsense.exception.GroupAccessDeniedException;
import com.spendsense.exception.GroupNotFoundException;
import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupMember;
import com.spendsense.group.entity.GroupRole;
import com.spendsense.group.repository.GroupMemberRepository;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GroupAccessService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupAccessService(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    public Group requireMember(
            UUID groupId,
            User user
    ) {
        return requireMembership(groupId, user)
                .getGroup();
    }

    public Group requireOwner(
            UUID groupId,
            User user
    ) {
        GroupMember membership =
                requireMembership(groupId, user);

        if (membership.getRole() != GroupRole.OWNER) {
            throw new GroupAccessDeniedException(
                    "Only the group owner can perform this action"
            );
        }

        return membership.getGroup();
    }

    public Group requireOwnerOrAdmin(
            UUID groupId,
            User user
    ) {
        GroupMember membership =
                requireMembership(groupId, user);

        if (membership.getRole() != GroupRole.OWNER
                && membership.getRole() != GroupRole.ADMIN) {

            throw new GroupAccessDeniedException(
                    "Only the group owner or admin can perform this action"
            );
        }

        return membership.getGroup();
    }

    private GroupMember requireMembership(
            UUID groupId,
            User user
    ) {
        Group group = groupRepository
                .findByGroupIdAndArchivedFalse(groupId)
                .orElseThrow(
                        () -> new GroupNotFoundException(
                                "Group not found"
                        )
                );

        return groupMemberRepository
                .findByGroupAndUser(group, user)
                .orElseThrow(
                        () -> new GroupNotFoundException(
                                "Group not found"
                        )
                );
    }
}
