package com.spendsense.category.repository;

import com.spendsense.category.entity.Category;
import com.spendsense.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByIsDefaultTrue();

    List<Category> findByCreatedBy(User user);

    boolean existsByNameAndCreatedBy(String name, User user);

}
