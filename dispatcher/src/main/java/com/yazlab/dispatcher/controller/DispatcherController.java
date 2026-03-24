package com.yazlab.dispatcher.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class DispatcherController {

    private final RestTemplate restTemplate;

    public DispatcherController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/users")
    public ResponseEntity<String> getUsers(HttpServletRequest request) {

        // 🔥 JWT'den gelen username
        String username = (String) request.getAttribute("username");

        // 🔥 HEADER OLUŞTUR
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User", username);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 🔥 exchange kullan (getForEntity değil!)
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8082/users",
                HttpMethod.GET,
                entity,
                String.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .header("Content-Type", "application/json")
                .body(response.getBody());
    }
}