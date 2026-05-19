package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.CategoryDTOS;
import io.github.fhv5.finsight.model.CategoryType;
import io.github.fhv5.finsight.model.User;
import io.github.fhv5.finsight.security.SecurityUser;
import io.github.fhv5.finsight.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private SecurityUser securityUser;
    private UUID userId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        securityUser = new SecurityUser(user);
        categoryId = UUID.randomUUID();
    }

    @Test
    void getCategoriesForCurrentUser_ShouldReturnOkStatusAndList() {
        CategoryDTOS.Response responseItem = CategoryDTOS.Response.builder()
                .id(categoryId)
                .name("Groceries")
                .type(CategoryType.GASTO)
                .build();
        List<CategoryDTOS.Response> expectedResponse = List.of(responseItem);

        when(categoryService.getCategoriesForCurrentUser(userId)).thenReturn(expectedResponse);

        ResponseEntity<List<CategoryDTOS.Response>> result = categoryController.getCategoriesForCurrentUser(securityUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    void getCategoryById_ShouldReturnOkStatusAndCategory() {
        CategoryDTOS.Response expectedResponse = CategoryDTOS.Response.builder()
                .id(categoryId)
                .name("Groceries")
                .type(CategoryType.GASTO)
                .build();

        when(categoryService.getCategoryById(categoryId, userId)).thenReturn(expectedResponse);

        ResponseEntity<CategoryDTOS.Response> result = categoryController.getCategoryById(securityUser, categoryId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    void createCategory_ShouldReturnCreatedStatusAndCategory() {
        CategoryDTOS.CreateRequest request = new CategoryDTOS.CreateRequest("Groceries", CategoryType.GASTO);
        CategoryDTOS.Response expectedResponse = CategoryDTOS.Response.builder()
                .id(categoryId)
                .name("Groceries")
                .type(CategoryType.GASTO)
                .build();

        when(categoryService.createCategory(eq(userId), any(CategoryDTOS.CreateRequest.class))).thenReturn(expectedResponse);

        ResponseEntity<CategoryDTOS.Response> result = categoryController.createCategory(securityUser, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    void updateCategory_ShouldReturnOkStatusAndUpdatedCategory() {
        CategoryDTOS.UpdateRequest request = new CategoryDTOS.UpdateRequest("Food");
        CategoryDTOS.Response expectedResponse = CategoryDTOS.Response.builder()
                .id(categoryId)
                .name("Food")
                .type(CategoryType.GASTO)
                .build();

        when(categoryService.updateCategory(eq(categoryId), eq(userId), any(CategoryDTOS.UpdateRequest.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<CategoryDTOS.Response> result = categoryController.updateCategory(securityUser, categoryId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    void deleteCategory_ShouldReturnNoContentStatus() {
        ResponseEntity<Void> result = categoryController.deleteCategory(securityUser, categoryId);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(categoryService).deleteCategory(categoryId, userId);
    }
}
