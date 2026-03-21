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
        String userServiceUrl = "http://localhost:8082/users";

        // 1. İsteği at ve cevabı al
        ResponseEntity<String> response = restTemplate.getForEntity(userServiceUrl, String.class);

        // 2. Arkadan gelen sorunlu başlıkları (Transfer-Encoding vb.) atıp,
        // sadece Body ve Status kodunu içeren temiz bir yanıt dönüyoruz.
        return ResponseEntity.status(response.getStatusCode())
                .header("Content-Type", "application/json")
                .body(response.getBody());
    }
}