package com.yazlab.dispatcher.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DispatcherController {

    @GetMapping("/users")
    public List<Map<String, String>> getUsers() {
        // Mock veri döndür
        return List.of(
            Map.of("username", "alp", "password", "1234"),
            Map.of("username", "ayse", "password", "abcd")
        );
    }
}