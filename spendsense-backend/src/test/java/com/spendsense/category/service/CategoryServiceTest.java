package com.spendsense.category.service;

import com.spendsense.category.dto.CategoryResponse;
import com.spendsense.category.dto.CreateCategoryRequest;
import com.spendsense.category.entity.Category;
import com.spendsense.category.repository.CategoryRepository;
import com.spendsense.exception.DuplicateCategoryException;
import com.spendsense.exception.GroupNotFoundException;
import com.spendsense.group.entity.Group;
import com.spendsense.group.entity.GroupMember;
import com.spendsense.group.entity.GroupRole;
import com.spendsense.group.repository.GroupMemberRepository;
import com.spendsense.group.repository.GroupRepository;
import com.spendsense.group.service.GroupAccessService;
import com.spendsense.security.CurrentUserService;
import com.spendsense.user.entity.User;
import com.spendsense.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private GroupAccessService groupAccessService;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Group group;
    private UUID groupId;

    @BeforeEach
    void setup(){

        this.user = new User();
        this.user.setEmail("swathi@gmail.com");

        groupId = UUID.randomUUID();

        group = new Group();
        group.setGroupId(groupId);
        group.setName("Family");

        when(groupAccessService.requireMember(groupId, user))
                .thenReturn(group);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);


    }

    @Test
    void shouldCreateCategorySuccessfully(){

        CreateCategoryRequest request =
                new CreateCategoryRequest(
                        "Pet Care",
                        null,
                        null
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(categoryRepository
                .existsByNameIgnoreCaseAndGroup(
                        "Pet Care",
                        group
                ))
                .thenReturn(false);


        Category savedCategory =
                new Category(
                        "Pet Care",
                        "category",
                        "#757575",
                        false,
                        group,
                        user
                );


        savedCategory.setCategoryId(UUID.randomUUID());

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        CategoryResponse response =
                categoryService.createCategory(groupId,request);

        assertNotNull(response);

        assertEquals(
                "Pet Care",
                response.getName()
        );

        verify(categoryRepository)
                .save(any(Category.class));
    }

    @Test
    void shouldNotCreateDuplicateCategory(){

        CreateCategoryRequest request =
                new CreateCategoryRequest(
                        "Pet Care",
                        null,
                        null
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(categoryRepository.existsByNameIgnoreCaseAndGroup(
                "Pet Care",
                group
        )).thenReturn(true);

        assertThrows(
                DuplicateCategoryException.class,
                () -> categoryService.createCategory(groupId,request)
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void shouldApplyDefaultIconAndColor(){
        CreateCategoryRequest request =
                new CreateCategoryRequest(
                        "Pet Care",
                        "",
                        ""
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(categoryRepository.existsByNameIgnoreCaseAndGroup(
                "Pet Care",
                group
        )).thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response =
                categoryService.createCategory(groupId, request);

        assertEquals(
                "category",
                response.getIcon()
        );

        assertEquals(
                "#757575",
                response.getColor()
        );
    }

    @Test
    void shouldGetCategoriesFromRequestedGroup() {

        Category foodCategory = new Category(
                "Food",
                "restaurant",
                "#4CAF50",
                true,
                group,
                user
        );

        foodCategory.setCategoryId(UUID.randomUUID());

        when(categoryRepository.findByGroupOrderByNameAsc(group))
                .thenReturn(List.of(foodCategory));

        List<CategoryResponse> responses =
                categoryService.getCategories(groupId);

        assertEquals(1, responses.size());

        assertEquals(
                "Food",
                responses.getFirst().getName()
        );

        assertEquals(
                group,
                foodCategory.getGroup()
        );

        verify(categoryRepository)
                .findByGroupOrderByNameAsc(group);
    }

    @Test
    void shouldRejectUserWhoIsNotGroupMember() {

        reset(groupAccessService);

        when(groupAccessService.requireMember(groupId, user))
                .thenThrow(
                        new GroupNotFoundException(
                                "Group not found"
                        )
                );

        assertThrows(
                GroupNotFoundException.class,
                () -> categoryService.getCategories(groupId)
        );

        verify(categoryRepository, never())
                .findByGroupOrderByNameAsc(any(Group.class));
    }
}
