package com.yazlab.dispatcher.controller;

import com.yazlab.dispatcher.config.ServiceUrlProperties;
import com.yazlab.dispatcher.http.HttpForwardClient;
import com.yazlab.dispatcher.http.ProxyBodyNormalizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RestController
public class AuthProxyController {

    private final RestTemplate restTemplate;
    private final ServiceUrlProperties serviceUrls;
    private final ObjectMapper objectMapper;

    public AuthProxyController(RestTemplate restTemplate,
                               ServiceUrlProperties serviceUrls,
                               ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.serviceUrls = serviceUrls;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<String> register(HttpServletRequest request) {
        try {
            byte[] json = ProxyBodyNormalizer.toJson(request, objectMapper);
            return HttpForwardClient.postJson(serviceUrls.getAuth() + "/auth/register", json, null);
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
            return HttpForwardClient.postJson(serviceUrls.getAuth() + "/auth/login", json, null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/auth/test")
    public ResponseEntity<String> authTest() {
        return restTemplate.exchange(
                serviceUrls.getAuth() + "/auth/test",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class);
    }
}
