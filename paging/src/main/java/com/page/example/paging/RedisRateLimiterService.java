package com.page.example.paging;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Service
@Primary
@ConditionalOnProperty(prefix = "app.rateLimiter", name = "type", havingValue = "redis")
public class RedisRateLimiterService implements RateLimiter {

    private final RedisTemplate<String, String> redis;
    private final int capacity;
    private final double refillPerMillis;
    private String script;
    private DefaultRedisScript<Object> redisScript;

    public RedisRateLimiterService(RedisTemplate<String, String> redis,
            @Value("${app.rateLimit.requests:30}") int maxRequests,
            @Value("${app.rateLimit.windowSeconds:60}") int windowSeconds) {
        this.redis = redis;
        this.capacity = maxRequests;
        this.refillPerMillis = ((double) maxRequests) / Math.max(1, windowSeconds) / 1000.0;
    }

    @PostConstruct
    void loadScript() throws Exception {
        ClassPathResource r = new ClassPathResource("redis/token_bucket.lua");
        try (InputStream in = r.getInputStream(); BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            script = sb.toString();
        }

        // prepare RedisScript wrapper to use the RedisTemplate.execute(script, keys, args...) helper
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        // script returns a multi (list) with numeric values; use Object.class and handle the returned container manually
        redisScript.setResultType(Object.class);
    }

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

    public boolean isAllowed(String clientId) {
        String key = "rate:bucket:" + clientId;
        long now = Instant.now().toEpochMilli();
        @SuppressWarnings("unchecked")
        Object raw = redis.execute((RedisScript<Object>) redisScript, Collections.singletonList(key),
                Long.toString(now), Integer.toString(capacity), Double.toString(refillPerMillis), "1");

        if (raw instanceof List) {
            List<?> res = (List<?>) raw;
            if (!res.isEmpty()) {
                Object first = res.get(0);
                if (first instanceof Number) {
                    return ((Number) first).longValue() == 1L;
                } else if (first instanceof byte[]) {
                    try {
                        String s = new String((byte[]) first, StandardCharsets.UTF_8);
                        return Long.parseLong(s) == 1L;
                    } catch (Exception ex) {
                        return false;
                    }
                } else if (first instanceof String) {
                    return Long.parseLong((String) first) == 1L;
                }
            }
        }
        return false;
    }
}
