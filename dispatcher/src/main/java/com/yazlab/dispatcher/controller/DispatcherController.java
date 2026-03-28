package com.yazlab.dispatcher.controller;

import com.yazlab.dispatcher.config.ServiceUrlProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class DispatcherController {

    private final RestTemplate restTemplate;
    private final ServiceUrlProperties serviceUrls;

    public DispatcherController(RestTemplate restTemplate, ServiceUrlProperties serviceUrls) {
        this.restTemplate = restTemplate;
        this.serviceUrls = serviceUrls;
    }

    @GetMapping("/users")
    public ResponseEntity<String> getUsers(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"error\":\"Bu işlem için yetkiniz yok\"}");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User", username);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                serviceUrls.getUser() + "/users",
                HttpMethod.GET,
                entity,
                String.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .header("Content-Type", "application/json")
                .body(response.getBody());
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getProfile(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User", username);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                serviceUrls.getUser() + "/users/me",
                HttpMethod.GET,
                entity,
                String.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .header("Content-Type", "application/json")
                .body(response.getBody());
    }
}
