package com.yazlab.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yazlab.dispatcher.config.ServiceUrlProperties;
import com.yazlab.dispatcher.http.GatewayHttpClient;
import com.yazlab.dispatcher.http.ProxyBodyNormalizer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class DispatcherController {

    private final GatewayHttpClient gatewayHttpClient;
    private final ServiceUrlProperties serviceUrls;
    private final ObjectMapper objectMapper;

    public DispatcherController(GatewayHttpClient gatewayHttpClient,
                                ServiceUrlProperties serviceUrls,
                                ObjectMapper objectMapper) {
        this.gatewayHttpClient = gatewayHttpClient;
        this.serviceUrls = serviceUrls;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/users")
    public ResponseEntity<String> getUsers(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        ResponseEntity<String> response = gatewayHttpClient.get(serviceUrls.getUser() + "/users", username);
        return ResponseEntity.status(response.getStatusCode())
                .header("Content-Type", "application/json")
                .body(response.getBody());
    }

    @PostMapping("/users")
    public ResponseEntity<String> createUser(HttpServletRequest request,
                                             @RequestBody(required = false) byte[] body) {
        try {
            byte[] json = ProxyBodyNormalizer.toJsonFromBytes(body, objectMapper);
            String username = request.getAttribute("username") != null
                    ? request.getAttribute("username").toString()
                    : null;
            ResponseEntity<String> r = gatewayHttpClient.postJson(
                    serviceUrls.getUser() + "/users", json, username);
            return ResponseEntity.status(r.getStatusCode())
                    .header("Content-Type", "application/json")
                    .body(r.getBody());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
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
