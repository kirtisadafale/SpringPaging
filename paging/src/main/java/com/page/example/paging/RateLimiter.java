package com.page.example.paging;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Abstraction for rate limiter implementations (DB-backed, Redis-backed, etc.).
 */
public interface RateLimiter {
    boolean isAllowed(HttpServletRequest request);
    boolean isAllowed(String clientId);
}
