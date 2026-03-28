package com.yazlab.dispatcher.controller;

import com.yazlab.dispatcher.config.ServiceUrlProperties;
import com.yazlab.dispatcher.http.GatewayHttpClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DispatcherController {

    private final GatewayHttpClient gatewayHttpClient;
    private final ServiceUrlProperties serviceUrls;

    public DispatcherController(GatewayHttpClient gatewayHttpClient, ServiceUrlProperties serviceUrls) {
        this.gatewayHttpClient = gatewayHttpClient;
        this.serviceUrls = serviceUrls;
    }

    @GetMapping("/users")
    public ResponseEntity<String> getUsers(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        ResponseEntity<String> response = gatewayHttpClient.get(serviceUrls.getUser() + "/users", username);
        return ResponseEntity.status(response.getStatusCode())
                .header("Content-Type", "application/json")
                .body(response.getBody());
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getProfile(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        ResponseEntity<String> response = gatewayHttpClient.get(serviceUrls.getUser() + "/users/me", username);
        return ResponseEntity.status(response.getStatusCode())
                .header("Content-Type", "application/json")
                .body(response.getBody());
    }
}
