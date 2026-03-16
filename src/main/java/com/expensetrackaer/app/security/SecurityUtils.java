package com.expensetrackaer.app.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    // ── Get current logged-in userId ──────────────────────────────
    // The userId is stored as credentials in the Authentication object
    // by JwtFilter after validating the token.
    // All service impls call this instead of hardcoded return 1L
    public static Long getCurrentUserId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            return (Long) token.getCredentials();
        }

        throw new IllegalStateException("No authenticated user found");
    }

    // ── Get current logged-in user email ──────────────────────────
    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}