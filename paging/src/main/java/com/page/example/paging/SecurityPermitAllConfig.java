package com.page.example.paging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test / dev friendly security config that permits all requests when NOT running
 * with the 'prod' profile. This prevents the auto-configured generated password
 * and 401/403 responses from interfering with unit/integration tests.
 */
@Configuration
@Conditional(SecurityPermitAllCondition.class)
//Spring Security forbids multiple filter chains that both match any request (anyRequest()). The test-global config created such a chain and so did SecurityPermitAllConfig. Adding @ConditionalOnMissingBean(SecurityFilterChain.class) ensures the main permit-all config only creates its chain if no other SecurityFilterChain is present (for example, the test auto-config). This keeps production behavior unchanged (it still activates via the SecurityPermitAllCondition when prod is not active) while preventing the duplicate chain in test contexts.
@ConditionalOnMissingBean(SecurityFilterChain.class)
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
