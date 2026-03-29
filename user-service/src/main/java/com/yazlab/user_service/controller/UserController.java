package com.yazlab.user_service.controller;

import com.yazlab.user_service.model.User;
import com.yazlab.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers(@RequestHeader(value = "X-User", required = false) String username) {

        logger.info("GET /users çağrıldı | kullanıcı: {}", username);

        return userService.getAllUsers();
    }

    @GetMapping("/me")
    public User getMe(@RequestHeader(value = "X-User", required = false) String username) {

        if (username == null) {
            logger.error("X-User header yok!");
            throw new RuntimeException("Kullanıcı bilgisi yok");
        }

        logger.info("GET /users/me çağrıldı | kullanıcı: {}", username);

        return userService.getUserByUsername(username);
    }
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.save(user));
    }

    
}
