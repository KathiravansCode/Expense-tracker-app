//package com.expensetrackaer.app.config;
//
//import com.expensetrackaer.app.entity.model.Category;
//import com.expensetrackaer.app.repository.CategoryRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class DatabaseSeeder implements ApplicationRunner {
//
//    private final CategoryRepository categoryRepository;
//
//    // ── Global default categories ─────────────────────────────────
//    // These are seeded ONCE in the DB and shared across ALL users.
//    // user_id = NULL means global — no user owns them.
//    // Users can create their own custom categories on top of these.
//    private static final List<String> GLOBAL_CATEGORIES = List.of(
//            // Income
//            "Salary",
//            "Freelance",
//            "Business",
//            "Other Income",
//            // Expense
//            "Food",
//            "Transport",
//            "Shopping",
//            "Bills",
//            "Healthcare",
//            "Other Expense"
//    );
//
//    @Autowired
//    public DatabaseSeeder(CategoryRepository categoryRepository) {
//        this.categoryRepository = categoryRepository;
//    }
//
//    @Override
//    public void run(ApplicationArguments args) {
//        seedGlobalCategories();
//    }
//
//    private void seedGlobalCategories() {
//
//        for (String name : GLOBAL_CATEGORIES) {
//
//            // Skip if already exists — safe to restart the app multiple times
//            if (!categoryRepository.existsByNameAndUserIsNull(name)) {
//
//                Category category = Category.builder()
//                        .name(name)
//                        .user(null) // ← NULL user_id = global category
//                        .build();
//
//                categoryRepository.save(category);
//            }
//        }
//    }
//}