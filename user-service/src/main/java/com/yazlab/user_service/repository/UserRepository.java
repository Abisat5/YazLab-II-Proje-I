package com.yazlab.user_service.repository;

import com.yazlab.user_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}