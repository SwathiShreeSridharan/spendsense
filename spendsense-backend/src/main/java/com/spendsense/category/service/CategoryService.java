package com.spendsense.category.service;

import com.spendsense.category.dto.CategoryResponse;
import com.spendsense.category.dto.CreateCategoryRequest;
import com.spendsense.category.entity.Category;
import com.spendsense.category.repository.CategoryRepository;
import com.spendsense.exception.DuplicateCategoryException;
import com.spendsense.exception.GroupNotFoundException;
import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupMember;
import com.spendsense.group.repository.GroupMemberRepository;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.group.service.GroupAccessService;
import com.spendsense.security.CurrentUserService;
import com.spendsense.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;
    private final GroupAccessService groupAccessService;

    public CategoryService(CategoryRepository categoryRepository, CurrentUserService currentUserService, GroupAccessService groupAccessService) {
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
        this.groupAccessService = groupAccessService;
    }

    public CategoryResponse createCategory(UUID groupId, CreateCategoryRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireMember(
                        groupId,
                        currentUser
                );

        String categoryName = request.getName().trim();

        boolean exists = categoryRepository
                .existsByNameIgnoreCaseAndGroup(
                        categoryName,
                        group
                );

        if(exists){
            throw new DuplicateCategoryException("Category already exists");
        }

        String icon = request.getIcon();

        if(icon == null || icon.isBlank()){
            icon = "category";
        }

        String color = request.getColor();

        if(color == null || color.isBlank()){
            color = "#757575";
        }

        Category category = new Category(
                categoryName,
                icon,
                color,
                false,
                group,
                currentUser
        );

        Category savedCategory = categoryRepository.save(category);

        return mapToResponse(savedCategory);
    }


    public List<CategoryResponse> getCategories(UUID groupId){

        User currentUser = currentUserService.getCurrentUser();

        Group group =
                groupAccessService.requireMember(
                        groupId,
                        currentUser
                );

        return categoryRepository
                .findByGroupOrderByNameAsc(group)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CategoryResponse mapToResponse(Category category){

        return new CategoryResponse(
                category.getCategoryId(),
                category.getName(),
                category.getIcon(),
                category.getColor(),
                category.isDefault()
        );
    }
}
