package com.yazlab.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yazlab.dispatcher.config.ServiceUrlProperties;
import com.yazlab.dispatcher.http.GatewayHttpClient;
import com.yazlab.dispatcher.http.ProxyBodyNormalizer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class AuthProxyController {

    private final GatewayHttpClient gatewayHttpClient;
    private final ServiceUrlProperties serviceUrls;
    private final ObjectMapper objectMapper;

    public AuthProxyController(GatewayHttpClient gatewayHttpClient,
                               ServiceUrlProperties serviceUrls,
                               ObjectMapper objectMapper) {
        this.gatewayHttpClient = gatewayHttpClient;
        this.serviceUrls = serviceUrls;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<String> register(HttpServletRequest request) {
        try {
            byte[] json = ProxyBodyNormalizer.toJson(request, objectMapper);
            return gatewayHttpClient.postJson(serviceUrls.getAuth() + "/auth/register", json, null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<String> login(HttpServletRequest request) {
        try {
            byte[] json = ProxyBodyNormalizer.toJson(request, objectMapper);
            return gatewayHttpClient.postJson(serviceUrls.getAuth() + "/auth/login", json, null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/auth/test")
    public ResponseEntity<String> authTest() {
        return gatewayHttpClient.get(serviceUrls.getAuth() + "/auth/test", null);
    }
}
