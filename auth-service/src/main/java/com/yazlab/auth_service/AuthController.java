package com.y.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/auth/test")
    public String test() {
        return "Auth Service calisiyor";
    }
}