package com.yazlab.dispatcher.config;

import com.yazlab.dispatcher.model.AccessRule;
import com.yazlab.dispatcher.repository.AccessRuleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Yetki kurallarini NoSQL uzerinden yonetir; bos koleksiyonda varsayilan kurallari yukler.
 */
@Component
@Profile("!test")
public class AccessRuleInitializer implements ApplicationRunner {

    private final AccessRuleRepository accessRuleRepository;

    public AccessRuleInitializer(AccessRuleRepository accessRuleRepository) {
        this.accessRuleRepository = accessRuleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (accessRuleRepository.count() > 0) {
            return;
        }
        accessRuleRepository.saveAll(defaultRules());
    }

    private static List<AccessRule> defaultRules() {
        return List.of(
                rule("USER", "GET", "/profile"),
                rule("USER", "GET", "/conversations"),
                rule("USER", "POST", "/conversations"),
                rule("USER", "GET", "/conversations/**"),
                rule("USER", "POST", "/conversations/*/messages"),
                rule("USER", "DELETE", "/conversations/*"),
                rule("ADMIN", "GET", "/users"),
                rule("ADMIN", "POST", "/users"),
                rule("ADMIN", "GET", "/users/**"));
    }

    private static AccessRule rule(String role, String method, String pattern) {
        return new AccessRule(null, role, method, pattern);
    }
}
