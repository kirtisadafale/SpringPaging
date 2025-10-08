package com.page.example.paging;

import javax.sql.DataSource;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
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
        // http
        //     .authorizeHttpRequests(authorize -> authorize
        //         // Allow unauthenticated clients to POST new movies (async enqueue endpoint)
        //         .requestMatchers(HttpMethod.POST, "/movies").permitAll()
        //         .anyRequest().authenticated()
        //     )
             http
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            
            // This is an API; disable CSRF (forms aren't used). If you prefer CSRF protection,
            // change this to ignore only the /movies POST endpoint instead of disabling globally.
            .csrf(csrf -> csrf.disable())
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
