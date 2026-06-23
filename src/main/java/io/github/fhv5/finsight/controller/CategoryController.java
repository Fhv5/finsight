package io.github.fhv5.finsight.controller;

import io.github.fhv5.finsight.dto.CategoryDTOS;
import io.github.fhv5.finsight.security.SecurityUser;
import io.github.fhv5.finsight.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/categories", version = "1")
@AllArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(
            summary = "Get all categories",
            description = "Retrieves a list of all categories for the currently authenticated user.",
            operationId = "getCategoriesForCurrentUser"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed")
            }
    )
    @GetMapping
    public ResponseEntity<List<CategoryDTOS.Response>> getCategoriesForCurrentUser(
            @AuthenticationPrincipal SecurityUser securityUser) {

        return new ResponseEntity<>(
                categoryService.getCategoriesForCurrentUser(securityUser.getId()),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Get category by ID",
            description = "Retrieves a specific category by its ID for the currently authenticated user.",
            operationId = "getCategoryById"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Category not found or does not belong to user")
            }
    )
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDTOS.Response> getCategoryById(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID categoryId
            ) {

        return new ResponseEntity<>(
                categoryService.getCategoryById(categoryId, securityUser.getId()),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Create a category",
            description = "Creates a new category for the currently authenticated user.",
            operationId = "createCategory"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Category created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "409", description = "Category name already exists for this user")
            }
    )
    @PostMapping
    public ResponseEntity<CategoryDTOS.Response> createCategory(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody CategoryDTOS.CreateRequest request) {

        return new ResponseEntity<>(
                categoryService.createCategory(securityUser.getId(), request),
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "Update a category",
            description = "Updates the name and/or type of an existing category for the currently authenticated user. " +
                    "A type change is rejected if the category has transactions associated to it.",
            operationId = "updateCategory"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Category updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Category not found or does not belong to user"),
                    @ApiResponse(responseCode = "409", description = "This user already has a category with the same name and type"),
                    @ApiResponse(responseCode = "422", description = "Cannot change type: category has existing transactions")
            }
    )
    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryDTOS.Response> updateCategory(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryDTOS.UpdateRequest request
            ) {

        return new ResponseEntity<>(
                categoryService.updateCategory(categoryId, securityUser.getId(), request),
                HttpStatus.OK
        );
    }

    @Operation(
            summary = "Delete a category",
            description = "Deletes a specific category by its ID for the currently authenticated user. " +
                    "Deletion is rejected if the category has transactions associated to it.",
            operationId = "deleteCategory"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "404", description = "Category not found or does not belong to user"),
                    @ApiResponse(responseCode = "422", description = "Cannot delete: category has existing transactions")
            }
    )
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId, securityUser.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
