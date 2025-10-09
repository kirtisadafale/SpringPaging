package com.page.example.paging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class RedisRateLimiterIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2.2")
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", () -> redis.getHost());
        registry.add("spring.redis.port", () -> redis.getFirstMappedPort());
    }

    @Autowired
    private RateLimiter rateLimiter;

    @Test
    public void testRedisLimiter_allows_and_blocks_correctly() {
        // The test properties set a small capacity (5 per window)
        String client = "test-client-1";

        // First N requests should be allowed
        int allowed = 0;
        for (int i = 0; i < 5; i++) {
            boolean ok = rateLimiter.isAllowed(client);
            if (ok) allowed++;
        }
        assertThat(allowed).isEqualTo(5);

        // Next request should be denied (exhausted)
        boolean sixth = rateLimiter.isAllowed(client);
        assertThat(sixth).isFalse();
    }
}
