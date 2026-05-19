package io.github.fhv5.finsight.service;

import io.github.fhv5.finsight.dto.CategoryDTOS;
import io.github.fhv5.finsight.exception.InvalidInputException;
import io.github.fhv5.finsight.exception.ResourceAlreadyExistsException;
import io.github.fhv5.finsight.exception.ResourceNotFoundException;
import io.github.fhv5.finsight.model.Category;
import io.github.fhv5.finsight.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryDTOS.Response> getCategoriesForCurrentUser(
            UUID userId
    ) {
        List<Category> categories = categoryRepository.findAllByUserId(userId);

        return categories.stream()
                .map(
                        category -> CategoryDTOS.Response.builder()
                                .id(category.getId())
                                .name(category.getName())
                                .type(category.getType())
                                .build())
                .toList();
    }

    public CategoryDTOS.Response getCategoryById(
            UUID categoryId,
            UUID userId) {

        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or does not belong to user"));

        return CategoryDTOS.Response.builder()
                .id(categoryId)
                .name(category.getName())
                .type(category.getType())
                .build();
    }

    public CategoryDTOS.Response createCategory(
            UUID userId,
            CategoryDTOS.CreateRequest request) {

        if (categoryRepository.existsByUserIdAndNameAndType(userId, request.name(), request.type())) {
            throw new ResourceAlreadyExistsException("Category name already exists for this user");
        }

        Category newCategory = Category.builder()
                .name(request.name())
                .type(request.type())
                .build();

        Category savedCategory = categoryRepository.save(newCategory);

        return CategoryDTOS.Response.builder()
                .id(savedCategory.getId())
                .name(savedCategory.getName())
                .type(savedCategory.getType())
                .build();
    }

    public CategoryDTOS.Response updateCategory(
            UUID categoryId,
            UUID userId,
            CategoryDTOS.UpdateRequest request) {

        Category existingCategory = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or does not belong to user"));

        if (request.name() != null && request.name().isBlank()) {
            throw new InvalidInputException("Category name cannot be empty");
        }

        if (request.name() != null && !request.name().equals(existingCategory.getName())) {
            if (categoryRepository.existsByUserIdAndNameAndType(userId, request.name(), existingCategory.getType())) {
                throw new ResourceAlreadyExistsException("This user already has a category with the same name and type");
            }

            existingCategory.setName(request.name());
        }

        Category savedCategory = categoryRepository.save(existingCategory);

        return CategoryDTOS.Response.builder()
                .id(savedCategory.getId())
                .name(savedCategory.getName())
                .type(savedCategory.getType())
                .build();
    }

    public void deleteCategory(UUID categoryId, UUID userId) {
        Category existingCategory = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or does not belong to user"));

        categoryRepository.delete(existingCategory);
    }

}
