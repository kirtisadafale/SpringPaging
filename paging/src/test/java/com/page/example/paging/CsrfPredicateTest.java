package com.page.example.paging;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsrfPredicateTest {

    @Test
    void csrfIgnore_acceptsPostMovies() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getMethod()).thenReturn("POST");
        when(req.getServletPath()).thenReturn("/movies");

        RequestMatcher matcher = SecurityConfig.csrfIgnore();
        assertTrue(matcher.matches(req));
    }

    @Test
    void csrfIgnore_rejectsPostOther() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getMethod()).thenReturn("POST");
        when(req.getServletPath()).thenReturn("/other");

        RequestMatcher matcher = SecurityConfig.csrfIgnore();
        assertFalse(matcher.matches(req));
    }
}
