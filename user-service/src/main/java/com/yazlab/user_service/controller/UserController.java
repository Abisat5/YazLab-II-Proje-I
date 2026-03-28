package com.yazlab.user_service.controller;

import com.yazlab.user_service.model.User;
import com.yazlab.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers(@RequestHeader("X-User") String username) {

        System.out.println("Gelen kullanıcı: " + username);

        return userService.getAllUsers();
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMe(@RequestHeader("X-User") String username) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}