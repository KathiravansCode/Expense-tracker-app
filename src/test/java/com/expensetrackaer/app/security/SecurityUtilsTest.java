package com.expensetrackaer.app.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_readsCredentialsFromAuthToken() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("a@example.com", 123L));

        assertThat(SecurityUtils.getCurrentUserId()).isEqualTo(123L);
        assertThat(SecurityUtils.getCurrentUserEmail()).isEqualTo("a@example.com");
    }

    @Test
    void getCurrentUserId_whenNoAuth_throws() {
        assertThatThrownBy(SecurityUtils::getCurrentUserId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No authenticated user");
    }
}

