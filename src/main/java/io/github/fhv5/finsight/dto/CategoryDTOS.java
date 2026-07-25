package io.github.fhv5.finsight.dto;

import io.github.fhv5.finsight.model.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.UUID;

@Schema(description = "Container for Category Data Transfer Objects")
public class CategoryDTOS {

    @Builder
    @Schema(description = "Category response representation")
    public record Response(
            @Schema(description = "Unique identifier of the category", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
            UUID id,
            @Schema(description = "Category name", example = "Groceries")
            String name,
            @Schema(description = "Type of category", example = "GASTO")
            CategoryType type
    ) {}

    @Schema(description = "Request payload to create a category")
    public record CreateRequest(
       @Schema(description = "Category name", example = "Groceries", requiredMode = Schema.RequiredMode.REQUIRED)
       @NotEmpty String name,
       @Schema(description = "Category type", example = "GASTO", requiredMode = Schema.RequiredMode.REQUIRED)
       @NotEmpty CategoryType type
    ) {}

    @Schema(description = "Request payload to update a category")
    public record UpdateRequest(
        @Schema(description = "Updated category name", example = "Supermarket", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty String name,
        @Schema(description = "Updated category type", example = "GASTO")
        CategoryType type
    ) {}
}
