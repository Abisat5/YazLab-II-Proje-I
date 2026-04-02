package com.yazlab.dispatcher.controller;

import com.yazlab.dispatcher.model.AccessRule;
import com.yazlab.dispatcher.repository.AccessRuleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
@DeleteMapping("/access-rules/{id}")
public ResponseEntity<Void> deleteAccessRule(@PathVariable String id) {
    if (!accessRuleRepository.existsById(id)) {
        return ResponseEntity.notFound().build();
    }
    accessRuleRepository.deleteById(id);
    return ResponseEntity.noContent().build();
}
}

