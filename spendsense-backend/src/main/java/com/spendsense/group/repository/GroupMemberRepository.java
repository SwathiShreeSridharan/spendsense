package com.spendsense.group.repository;

import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupMember;
import com.spendsense.group.entity.GroupRole;
import com.spendsense.user.entity.User;
import org.hibernate.sql.exec.spi.JdbcCallParameterExtractor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    List<GroupMember> findByUser(User user);

    List<GroupMember> findByGroup(Group group);

    Optional<GroupMember> findByGroupAndUser(
            Group group,
            User user
    );

    boolean existsByGroupAndUser(
            Group group,
            User user
    );

    List<GroupMember> findByGroupAndRole(
            Group group,
            GroupRole role
    );

    void deleteByGroupAndUser(
            Group group,
            User user
    );

    List<GroupMember> findByUserAndGroupArchivedFalse(User user);
}
