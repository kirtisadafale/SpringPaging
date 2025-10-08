package com.page.example.paging;

import javax.sql.DataSource;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

@Configuration
@Profile("prod")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Allow unauthenticated users to create new movies (POST /movies)
                // This is needed to allow clients to enqueue new movies for processing.
                .requestMatchers(HttpMethod.POST, "/movies").permitAll()
                .anyRequest().authenticated()
            )
            
            // Keep CSRF enabled, but ignore it for the public POST /movies endpoint only.
            // This reduces attack surface compared to disabling CSRF globally.
            .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/movies", "POST")))
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // Use JDBC-based user store: users and authorities will be loaded from the DataSource (MySQL in prod)
    @Bean
    public UserDetailsManager users(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        // Use the default Spring Security table layout (users + authorities). If you use a different schema,
        // configure the queries here with manager.setUsersByUsernameQuery(...) and setAuthoritiesByUsernameQuery(...).
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
