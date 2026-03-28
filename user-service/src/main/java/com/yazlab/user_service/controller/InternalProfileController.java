package com.yazlab.user_service.controller;

import com.yazlab.user_service.model.User;
import com.yazlab.user_service.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/internal")
public class InternalProfileController {

    private final UserRepository userRepository;

    public InternalProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Auth servisinden kayit sonrasi cagrılır (dahili token zorunlu). */
    @PostMapping("/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public void createProfile(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username gerekli");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        userRepository.save(new User(username, ""));
    }
}
