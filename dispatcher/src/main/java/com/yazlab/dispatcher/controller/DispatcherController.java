package com.yazlab.dispatcher.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class DispatcherController {

    private final RestTemplate restTemplate;

    // Dependency Injection ile RestTemplate'i içeri alıyoruz
    public DispatcherController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/users")
    public ResponseEntity<String> getUsers() {
        // Gelen isteği al, 8082 portunda çalışan User Service'e gönder ve cevabı dön!
        String userServiceUrl = "http://localhost:8082/users";
        return restTemplate.getForEntity(userServiceUrl, String.class);
    }
}