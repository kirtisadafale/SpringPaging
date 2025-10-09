package com.page.example.paging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

class RedisRateLimiterServiceTest {

    @Mock
    RedisTemplate<String, String> redis;

    RedisRateLimiterService svc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        svc = new RedisRateLimiterService(redis, 5, 1); // 5 requests per 1s window (small)
        try {
            svc.loadScript();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void allowed_when_script_returns_allowed() {
        // script returns [1, tokens]
        List<Object> scriptRes = Arrays.asList(1L, 4L);
        when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(scriptRes);

        boolean ok = svc.isAllowed("client-a");
        assertTrue(ok);
    }

    @Test
    void denied_when_script_returns_zero() {
        List<Object> scriptRes = Arrays.asList(0L, 0L);
        when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(scriptRes);

        boolean ok = svc.isAllowed("client-b");
        assertFalse(ok);
    }

    @Test
    void denied_when_script_returns_null() {
        when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(null);
        assertFalse(svc.isAllowed("x"));
    }
}
