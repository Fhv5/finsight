package io.github.fhv5.finsight.dto;

import io.github.fhv5.finsight.model.CategoryType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.UUID;

public class CategoryDTOS {

    @Builder
    public record Response(
            UUID id,
            String name,
            CategoryType type
    ) {}

    public record CreateRequest(
       @NotEmpty String name,
       @NotEmpty CategoryType type
    ) {}

    public record UpdateRequest(
        @NotEmpty String name,
        CategoryType type
    ) {}
}
