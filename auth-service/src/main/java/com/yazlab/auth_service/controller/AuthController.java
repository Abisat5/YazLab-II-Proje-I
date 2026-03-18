package com.yazlab.auth_service.controller;
import java.util.Map;
import com.yazlab.auth_service.dto.RegisterRequest;
import com.yazlab.auth_service.model.User;
import com.yazlab.auth_service.service.AuthService;
import org.springframework.web.bind.annotation.*;
import com.yazlab.auth_service.dto.LoginRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/test")
    public String test(){
        return "Auth service calisiyorr";
    }

@PostMapping("/login")
public Map<String, String> login(@RequestBody LoginRequest request) {
    String token = authService.login(request);
    return Map.of("token", token);
}
}
