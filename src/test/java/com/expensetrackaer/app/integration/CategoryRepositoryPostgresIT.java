package com.expensetrackaer.app.integration;

import com.expensetrackaer.app.entity.model.Category;
import com.expensetrackaer.app.entity.model.User;
import com.expensetrackaer.app.repository.CategoryRepository;
import com.expensetrackaer.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("it")
class CategoryRepositoryPostgresIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("expense_it")
            .withUsername("it_user")
            .withPassword("it_pass");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void findAllAccessibleByUser_returnsGlobalAndUserOwned() {
        User user = userRepository.save(User.builder()
                .name("A")
                .email("a@example.com")
                .password("x")
                .build());

        Category global = categoryRepository.save(Category.builder()
                .name("Food")
                .user(null)
                .build());

        Category owned = categoryRepository.save(Category.builder()
                .name("Custom")
                .user(user)
                .build());

        List<Category> accessible = categoryRepository.findAllAccessibleByUser(user.getId());

        assertThat(accessible)
                .extracting(Category::getId)
                .contains(global.getId(), owned.getId());
    }

    @Test
    void findAccessibleCategory_allowsGlobal_butRejectsOtherUsersCategory() {
        User user1 = userRepository.save(User.builder()
                .name("U1")
                .email("u1@example.com")
                .password("x")
                .build());
        User user2 = userRepository.save(User.builder()
                .name("U2")
                .email("u2@example.com")
                .password("x")
                .build());

        Category global = categoryRepository.save(Category.builder()
                .name("Food")
                .user(null)
                .build());

        Category user2Cat = categoryRepository.save(Category.builder()
                .name("U2Only")
                .user(user2)
                .build());

        assertThat(categoryRepository.findAccessibleCategory(global.getId(), user1.getId()))
                .isPresent();
        assertThat(categoryRepository.findAccessibleCategory(user2Cat.getId(), user1.getId()))
                .isEmpty();
    }
}
