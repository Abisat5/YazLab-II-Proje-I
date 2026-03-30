package com.yazlab.dispatcher.controller;

import com.yazlab.dispatcher.model.AccessRule;
import com.yazlab.dispatcher.repository.AccessRuleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AccessRuleController {

    private final AccessRuleRepository accessRuleRepository;

    public AccessRuleController(AccessRuleRepository accessRuleRepository) {
        this.accessRuleRepository = accessRuleRepository;
    }

    @GetMapping("/access-rules")
    public List<AccessRule> getAccessRules() {
        return accessRuleRepository.findAll();
    }
}

