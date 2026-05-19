package io.github.fhv5.finsight.repository;

import io.github.fhv5.finsight.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoriesRepository extends JpaRepository<Category, UUID> {
}
