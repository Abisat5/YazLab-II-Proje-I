package com.yazlab.dispatcher.repository;

import com.yazlab.dispatcher.model.AccessRule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AccessRuleRepository extends MongoRepository<AccessRule, String> {

    List<AccessRule> findByRole(String role);
}
