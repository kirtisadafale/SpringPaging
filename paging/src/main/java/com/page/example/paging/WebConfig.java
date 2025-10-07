package com.page.example.paging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.Filter;
import org.slf4j.MDC;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Configuration
public class WebConfig {

    // Register ForwardedHeaderFilter so ServletUriComponentsBuilder and other
    // utilities will honor X-Forwarded-* / Forwarded headers when building URLs.
    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    // Correlation ID filter: ensures every request has an X-Correlation-ID, stores it in MDC
    @Bean
    public Filter correlationIdFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                String headerName = "X-Correlation-ID";
                String correlationId = request.getHeader(headerName);
                if (correlationId == null || correlationId.isBlank()) {
                    correlationId = UUID.randomUUID().toString();
                }
                MDC.put("correlationId", correlationId);
                response.setHeader(headerName, correlationId);
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    MDC.remove("correlationId");
                }
            }
        };
    }

}
