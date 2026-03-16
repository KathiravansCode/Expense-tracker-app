package com.expensetrackaer.app.repository;

import com.expensetrackaer.app.entity.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ── Used by getCategories() ───────────────────────────────────
    // Returns global categories (user_id IS NULL) + user's own custom categories
    @Query("""
            SELECT c FROM Category c
            WHERE c.user IS NULL
            OR c.user.id = :userId
            """)
    List<Category> findAllAccessibleByUser(Long userId);

    // ── Used by createCategory() duplicate check ──────────────────
    // Checks if user already has a custom category with the same name
    boolean existsByNameAndUserId(String name, Long userId);

    // ── Used by updateCategory() and deleteCategory() ────────────
    // Only fetches the user's own custom categories — global ones are excluded
    // so the service can reject attempts to edit/delete global categories
    Optional<Category> findByIdAndUserId(Long id, Long userId);

    // ── Used by createTransaction() and createBudget() ───────────
    // A category is accessible if it's global OR belongs to the user
    @Query("""
            SELECT c FROM Category c
            WHERE c.id = :id
            AND (c.user IS NULL OR c.user.id = :userId)
            """)
    Optional<Category> findAccessibleCategory(Long id, Long userId);

    // ── Used by DatabaseSeeder to avoid duplicate global categories ─
    boolean existsByNameAndUserIsNull(String name);

    // ── Used by deleteCategory() ──────────────────────────────────
    boolean existsById(Long categoryId);
}