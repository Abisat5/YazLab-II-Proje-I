package com.yazlab.dispatcher.service;

import com.yazlab.dispatcher.model.AccessRule;
import com.yazlab.dispatcher.repository.AccessRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Service
public class MongoAccessAuthorizationService implements AccessAuthorizationService {

    private final AccessRuleRepository accessRuleRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public MongoAccessAuthorizationService(AccessRuleRepository accessRuleRepository) {
        this.accessRuleRepository = accessRuleRepository;
    }

    @Override
    public boolean canAccess(String role, String httpMethod, String requestPath) {
        if (role == null || role.isBlank()) {
            return false;
        }
        if ("ADMIN".equals(role)) {
            return matchesRules("ADMIN", httpMethod, requestPath)
                    || matchesRules("USER", httpMethod, requestPath);
        }
        return matchesRules(role, httpMethod, requestPath);
    }

    private boolean matchesRules(String role, String httpMethod, String requestPath) {
        List<AccessRule> rules = accessRuleRepository.findByRole(role);
        for (AccessRule rule : rules) {
            if (!methodMatches(rule.getHttpMethod(), httpMethod)) {
                continue;
            }
            if (pathMatcher.match(rule.getPathPattern(), requestPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean methodMatches(String ruleMethod, String requestMethod) {
        if (ruleMethod == null || "*".equals(ruleMethod)) {
            return true;
        }
        return ruleMethod.equalsIgnoreCase(requestMethod);
    }
}
