package com.yazlab.auth_service.repository;

import com.yazlab.auth_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findFirstByUsername(String username);

}