package com.page.example.paging;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class SecurityPermitAllCondition implements Condition, EnvironmentAware {

    private Environment env;

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (env == null) {
            env = context.getEnvironment();
        }
        // Enable when 'prod' is NOT active
        for (String profile : env.getActiveProfiles()) {
            if ("prod".equals(profile)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }
}
