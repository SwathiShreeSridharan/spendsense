package com.spendsense.group.repository;

import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupType;
import com.spendsense.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    Optional<Group> findByGroupIdAndArchivedFalse(UUID groupId);

    Optional<Group> findByGroupIdAndCreatedByAndArchivedFalse(
            UUID groupId,
            User createdBy
    );

    Optional<Group> findByCreatedByAndGroupTypeAndArchivedFalse(
            User createdBy,
            GroupType groupType
    );

    boolean existsByCreatedByAndNameIgnoreCaseAndArchivedFalse(
            User createdBy,
            String name
    );

    List<Group> findByCreatedByAndArchivedFalse(User createdBy);

}
