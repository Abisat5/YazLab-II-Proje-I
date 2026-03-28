package com.yazlab.user_service.controller;

import com.yazlab.user_service.model.User;
import com.yazlab.user_service.service.UserService;
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
    public List<User> getUsers(@RequestHeader(value = "X-User", required = false) String username) {

        System.out.println("İstek atan kullanıcı: " + username);

        return userService.getAllUsers();
    }

    @GetMapping("/me")
    public User getMe(@RequestHeader(value = "X-User", required = false) String username) {

        if (username == null) {
            throw new RuntimeException("Kullanıcı bilgisi bulunamadı (X-User header yok)");
        }

        System.out.println("Profil isteği: " + username);

        return userService.getUserByUsername(username);
    }
}