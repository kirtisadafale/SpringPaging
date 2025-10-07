package com.page.example.paging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test / dev friendly security config that permits all requests when NOT running
 * with the 'prod' profile. This prevents the auto-configured generated password
 * and 401/403 responses from interfering with unit/integration tests.
 */
@Configuration
@Conditional(SecurityPermitAllCondition.class)
@Order(0)
public class SecurityPermitAllConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
