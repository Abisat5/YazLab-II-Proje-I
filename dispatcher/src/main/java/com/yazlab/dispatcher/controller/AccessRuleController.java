package com.yazlab.dispatcher.controller;

import com.yazlab.dispatcher.model.AccessRule;
import com.yazlab.dispatcher.repository.AccessRuleRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @DeleteMapping("/access-rules/{id}")
    public ResponseEntity<Void> deleteAccessRule(@PathVariable String id) {
        if (!accessRuleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        accessRuleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/access-rules")
    public ResponseEntity<AccessRule> createAccessRule(@Valid @RequestBody AccessRule request) {
        AccessRule toSave = new AccessRule(
                null,
                request.getRole(),
                request.getHttpMethod(),
                request.getPathPattern()
        );
        AccessRule saved = accessRuleRepository.save(toSave);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}

