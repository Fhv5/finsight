package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.CategoryDTOS;
import io.github.fhv5.finsight.exception.ExistingBudgetException;
import io.github.fhv5.finsight.exception.ExistingTransactionsException;
import io.github.fhv5.finsight.exception.InvalidInputException;
import io.github.fhv5.finsight.exception.ResourceAlreadyExistsException;
import io.github.fhv5.finsight.exception.ResourceNotFoundException;
import io.github.fhv5.finsight.model.Category;
import io.github.fhv5.finsight.model.CategoryType;
import io.github.fhv5.finsight.repository.BudgetRepository;
import io.github.fhv5.finsight.repository.CategoryRepository;
import io.github.fhv5.finsight.repository.TransactionRepository;
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

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetRepository budgetRepository;

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
    void createCategory_ShouldSaveAndReturnCategory_WhenValid() {
        CategoryDTOS.CreateRequest request = new CategoryDTOS.CreateRequest("Transport", CategoryType.GASTO);

        when(categoryRepository.existsByUserIdAndNameAndType(userId, "Transport", CategoryType.GASTO)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(UUID.randomUUID());
            return category;
        });

        CategoryDTOS.Response response = categoryService.createCategory(userId, request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals("Transport", response.name());
        assertEquals(CategoryType.GASTO, response.type());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldThrowException_WhenNameAlreadyExistsForType() {
        CategoryDTOS.CreateRequest request = new CategoryDTOS.CreateRequest("Groceries", CategoryType.GASTO);

        when(categoryRepository.existsByUserIdAndNameAndType(userId, "Groceries", CategoryType.GASTO)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> categoryService.createCategory(userId, request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldUpdateAndReturnCategory_WhenValid() {
        CategoryDTOS.UpdateRequest request = new CategoryDTOS.UpdateRequest("Food", null);

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
        CategoryDTOS.UpdateRequest request = new CategoryDTOS.UpdateRequest("   ", null);

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));

        assertThrows(InvalidInputException.class, () -> categoryService.updateCategory(categoryId, userId, request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldThrowException_WhenNameAlreadyExistsForType() {
        CategoryDTOS.UpdateRequest request = new CategoryDTOS.UpdateRequest("Transport", null);

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));
        when(categoryRepository.existsByUserIdAndNameAndType(userId, "Transport", CategoryType.GASTO)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> categoryService.updateCategory(categoryId, userId, request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldNotUpdate_WhenNameIsUnchanged() {
        CategoryDTOS.UpdateRequest request = new CategoryDTOS.UpdateRequest("Groceries", null);

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);

        CategoryDTOS.Response response = categoryService.updateCategory(categoryId, userId, request);

        assertNotNull(response);
        // existsByUserIdAndNameAndType should NOT be called when the name is unchanged
        verify(categoryRepository, never()).existsByUserIdAndNameAndType(any(), any(), any());
        verify(categoryRepository).save(mockCategory);
    }

    @Test
    void updateCategory_ShouldThrowException_WhenTypeChangeHasExistingTransactions() {
        // Request to change type from GASTO to INGRESO, but transactions exist for this category
        CategoryDTOS.UpdateRequest request = new CategoryDTOS.UpdateRequest(null, CategoryType.INGRESO);

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));
        when(transactionRepository.existsByCategoryIdAndUserId(categoryId, userId)).thenReturn(true);

        assertThrows(ExistingTransactionsException.class, () -> categoryService.updateCategory(categoryId, userId, request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldUpdateType_WhenNoExistingTransactions() {
        CategoryDTOS.UpdateRequest request = new CategoryDTOS.UpdateRequest(null, CategoryType.INGRESO);

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));
        when(transactionRepository.existsByCategoryIdAndUserId(categoryId, userId)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);

        CategoryDTOS.Response response = categoryService.updateCategory(categoryId, userId, request);

        assertNotNull(response);
        assertEquals(CategoryType.INGRESO, mockCategory.getType());
        verify(categoryRepository).save(mockCategory);
    }

    @Test
    void deleteCategory_ShouldDelete_WhenValid() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));
        when(transactionRepository.existsByCategoryIdAndUserId(categoryId, userId)).thenReturn(false);
        when(budgetRepository.existsByCategoryIdAndUserId(categoryId, userId)).thenReturn(false);

        categoryService.deleteCategory(categoryId, userId);

        verify(categoryRepository).delete(mockCategory);
    }

    @Test
    void deleteCategory_ShouldThrowException_WhenNotFound() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(categoryId, userId));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldThrowException_WhenTransactionsExist() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));
        when(transactionRepository.existsByCategoryIdAndUserId(categoryId, userId)).thenReturn(true);

        assertThrows(ExistingTransactionsException.class, () -> categoryService.deleteCategory(categoryId, userId));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldThrowException_WhenActiveBudgetExists() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(mockCategory));
        when(transactionRepository.existsByCategoryIdAndUserId(categoryId, userId)).thenReturn(false);
        when(budgetRepository.existsByCategoryIdAndUserId(categoryId, userId)).thenReturn(true);

        assertThrows(ExistingBudgetException.class, () -> categoryService.deleteCategory(categoryId, userId));
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
