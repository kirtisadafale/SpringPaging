package com.page.example.paging;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Simple in-memory rate limiter keyed by a client id (IP address or other identifier).
 *
 * - Configurable via properties: app.rateLimit.requests and app.rateLimit.windowSeconds
 * - Not distributed. Intended for basic protection from accidental or low-effort abuse.
 */
@Service
public class RateLimiterService {

    private final int maxRequests;
    private final int windowSeconds;

    private final ConcurrentMap<String, SlidingWindow> buckets = new ConcurrentHashMap<>();

    public RateLimiterService(
            @Value("${app.rateLimit.requests:30}") int maxRequests,
            @Value("${app.rateLimit.windowSeconds:60}") int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    public boolean isAllowed(String clientId) {
        if (clientId == null) clientId = "-unknown-";
        SlidingWindow bucket = buckets.computeIfAbsent(clientId, k -> new SlidingWindow(maxRequests, windowSeconds));
        return bucket.allowRequest();
    }

    public boolean isAllowed(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String client = null;
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For may contain a comma separated list; the originating client is the first one
            client = forwarded.split(",")[0].trim();
        }
        if (client == null || client.isBlank()) {
            client = request.getRemoteAddr();
        }
        return isAllowed(client);
    }

    // small sliding-window implementation
    private static class SlidingWindow {
        private final int max;
        private final int windowSeconds;
        private final Deque<Long> timestamps = new ArrayDeque<>();

        SlidingWindow(int max, int windowSeconds) {
            this.max = max;
            this.windowSeconds = windowSeconds;
        }

        synchronized boolean allowRequest() {
            long now = Instant.now().getEpochSecond();
            long cutoff = now - windowSeconds;
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.removeFirst();
            }
            if (timestamps.size() < max) {
                timestamps.addLast(now);
                return true;
            }
            return false;
        }
    }
}
