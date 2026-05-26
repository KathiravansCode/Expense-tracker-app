package com.expensetrackaer.app.testutil;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityTestUtils {

    private SecurityTestUtils() {}

    public static void setAuthenticatedUser(Long userId, String email) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(email, userId));
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}

