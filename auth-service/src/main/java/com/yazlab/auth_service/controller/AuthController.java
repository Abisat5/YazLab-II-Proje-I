package com.yazlab.auth_service.controller;

import com.yazlab.auth_service.dto.LoginRequest;
import com.yazlab.auth_service.dto.RegisterRequest;
import com.yazlab.auth_service.model.User;
import com.yazlab.auth_service.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return Map.of("token", token);
    }

    @GetMapping("/test")
    public String test() {
        return "Auth service calisiyor";
    }
}