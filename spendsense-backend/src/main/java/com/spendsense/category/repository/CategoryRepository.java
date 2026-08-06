package com.spendsense.category.repository;

import com.spendsense.category.entity.Category;
import com.spendsense.group.entity.Group;
import com.spendsense.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNameIgnoreCaseAndGroup(
            String name,
            Group group
    );

    List<Category> findByGroupOrderByNameAsc(
            Group group
    );

    Optional<Category> findByCategoryIdAndGroup(
            UUID categoryId,
            Group group
    );

}
