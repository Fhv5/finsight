package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.CategoryDTOS;
import io.github.fhv5.finsight.exception.InvalidInputException;
import io.github.fhv5.finsight.exception.ResourceAlreadyExistsException;
import io.github.fhv5.finsight.exception.ResourceNotFoundException;
import io.github.fhv5.finsight.model.Category;
import io.github.fhv5.finsight.model.CategoryType;
import io.github.fhv5.finsight.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private UUID userId;
    private UUID categoryId;
    private Category mockCategory;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        mockCategory = Category.builder()
                .id(categoryId)
                .name("Groceries")
                .type(CategoryType.GASTO)
                .build();
    }

    @Test
    void getCategoriesForCurrentUser_ShouldReturnListOfCategories() {
        when(categoryRepository.findAllByUserId(userId)).thenReturn(List.of(mockCategory));

        List<CategoryDTOS.Response> categories = categoryService.getCategoriesForCurrentUser(userId);

        assertEquals(1, categories.size());
        assertEquals(categoryId, categories.getFirst().id());
        assertEquals("Groceries", categories.getFirst().name());
        assertEquals(CategoryType.GASTO, categories.getFirst().type());
        verify(categoryRepository).findAllByUserId(userId);
    }

    @Test
    void getCategoryById_ShouldReturnCategory_WhenValidIdAndUserId() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));

        CategoryDTOS.Response response = categoryService.getCategoryById(categoryId, userId);

        assertNotNull(response);
        assertEquals(categoryId, response.id());
        assertEquals("Groceries", response.name());
        verify(categoryRepository).findByIdAndUserId(categoryId, userId);
    }

    @Test
    void getCategoryById_ShouldThrowException_WhenNotFound() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(categoryId, userId));
    }

    @Test
    void updateCategory_ShouldUpdateAndReturnCategory_WhenValid() {
        CategoryDTOS.UpdateRequest request = new CategoryDTOS.UpdateRequest("Food");

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));
        when(categoryRepository.existsByUserIdAndNameAndType(userId, "Food", CategoryType.GASTO)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);

        CategoryDTOS.Response response = categoryService.updateCategory(categoryId, userId, request);

        assertNotNull(response);
        assertEquals("Food", mockCategory.getName());
        verify(categoryRepository).save(mockCategory);
    }

    @Test
    void updateCategory_ShouldThrowException_WhenNameIsBlank() {
        CategoryDTOS.UpdateRequest request = new CategoryDTOS.UpdateRequest("   ");

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));

        assertThrows(InvalidInputException.class, () -> categoryService.updateCategory(categoryId, userId, request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldThrowException_WhenNameAlreadyExistsForType() {
        CategoryDTOS.UpdateRequest request = new CategoryDTOS.UpdateRequest("Transport");

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));
        when(categoryRepository.existsByUserIdAndNameAndType(userId, "Transport", CategoryType.GASTO)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> categoryService.updateCategory(categoryId, userId, request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldNotUpdate_WhenNameIsUnchanged() {
        CategoryDTOS.UpdateRequest request = new CategoryDTOS.UpdateRequest("Groceries");

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);

        CategoryDTOS.Response response = categoryService.updateCategory(categoryId, userId, request);

        assertNotNull(response);
        // existsByUserIdAndNameAndType should NOT be called when the name is unchanged
        verify(categoryRepository, never()).existsByUserIdAndNameAndType(any(), any(), any());
        verify(categoryRepository).save(mockCategory);
    }

    @Test
    void deleteCategory_ShouldDelete_WhenValid() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));

        categoryService.deleteCategory(categoryId, userId);

        verify(categoryRepository).delete(mockCategory);
    }

    @Test
    void deleteCategory_ShouldThrowException_WhenNotFound() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(categoryId, userId));
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
