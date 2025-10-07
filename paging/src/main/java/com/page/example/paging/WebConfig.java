package com.page.example.paging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class WebConfig {

    // Register ForwardedHeaderFilter so ServletUriComponentsBuilder and other
    // utilities will honor X-Forwarded-* / Forwarded headers when building URLs.
    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

}
