package com.page.example.paging;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

/**
 * DB-backed token bucket rate limiter. Uses a JPA entity (TokenBucket) and a
 * pessimistic lock on the row to safely refill and consume tokens under
 * concurrent requests.
 *
 * Configuration (kept compatible with the previous properties):
 * - app.rateLimit.requests = capacity (default 30)
 * - app.rateLimit.windowSeconds = sliding window used to derive refill rate (default 60)
 */
@Service
public class RateLimiterService {

    private final int capacity;
    private final double refillPerSecond;
    private final TokenBucketRepository repo;

    public RateLimiterService(
            TokenBucketRepository repo,
            @Value("${app.rateLimit.requests:30}") int maxRequests,
            @Value("${app.rateLimit.windowSeconds:60}") int windowSeconds) {
        this.repo = repo;
        this.capacity = maxRequests;
        // refill so that the bucket refills `maxRequests` tokens every `windowSeconds`
        this.refillPerSecond = (double) maxRequests / Math.max(1, windowSeconds);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean isAllowed(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String client = null;
        if (forwarded != null && !forwarded.isBlank()) {
            client = forwarded.split(",")[0].trim();
        }
        if (client == null || client.isBlank()) {
            client = request.getRemoteAddr();
        }
        return isAllowed(client);
    }

    /**
     * Token-bucket check. Uses a pessimistic lock to avoid races and saves the
     * token bucket state within the same transaction.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean isAllowed(String clientId) {
        if (clientId == null) clientId = "-unknown-";

        Instant now = Instant.now();

        // Try to load with a pessimistic write lock. If absent, create a new bucket.
        Optional<TokenBucket> opt = repo.findByClientIdForUpdate(clientId);
        TokenBucket bucket;
        if (opt.isPresent()) {
            bucket = opt.get();
        } else {
            bucket = new TokenBucket(clientId, capacity, refillPerSecond, now);
            try {
                bucket = repo.save(bucket);
            } catch (DataIntegrityViolationException ex) {
                // rare race: another tx created the row; load it locked and continue
                opt = repo.findByClientIdForUpdate(clientId);
                if (opt.isPresent()) {
                    bucket = opt.get();
                } else {
                    // give up conservatively
                    return false;
                }
            }
        }

        // Refill tokens according to elapsed time
        double elapsedSeconds = Duration.between(bucket.getLastRefill(), now).toMillis() / 1000.0;
        if (elapsedSeconds > 0) {
            double added = elapsedSeconds * bucket.getRefillRate();
            double newTokens = Math.min(bucket.getCapacity(), bucket.getTokens() + added);
            bucket.setTokens(newTokens);
            bucket.setLastRefill(now);
        }

        if (bucket.getTokens() >= 1.0) {
            bucket.setTokens(bucket.getTokens() - 1.0);
            repo.save(bucket);
            return true;
        }

        repo.save(bucket);
        return false;
    }
}
