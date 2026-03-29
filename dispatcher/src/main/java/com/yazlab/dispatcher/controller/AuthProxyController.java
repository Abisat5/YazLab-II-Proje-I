package com.yazlab.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yazlab.dispatcher.config.ServiceUrlProperties;
import com.yazlab.dispatcher.dto.AuthCredentialsRequest;
import com.yazlab.dispatcher.http.GatewayHttpClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<String> register(@RequestBody(required = false) AuthCredentialsRequest credentials) {
        if (credentials == null || isBlank(credentials.getUsername())) {
            return jsonError(400, "Body'de JSON gonderin: {\"username\":\"...\",\"password\":\"...\"}");
        }
        try {
            byte[] json = objectMapper.writeValueAsBytes(credentials);
            return gatewayHttpClient.postJson(serviceUrls.getAuth() + "/auth/register", json, null);
        } catch (IOException e) {
            return jsonError(400, e.getMessage());
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@RequestBody(required = false) AuthCredentialsRequest credentials) {
        if (credentials == null || isBlank(credentials.getUsername())) {
            return jsonError(400, "Body'de JSON gonderin: {\"username\":\"...\",\"password\":\"...\"}");
        }
        try {
            byte[] json = objectMapper.writeValueAsBytes(credentials);
            return gatewayHttpClient.postJson(serviceUrls.getAuth() + "/auth/login", json, null);
        } catch (IOException e) {
            return jsonError(400, e.getMessage());
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static ResponseEntity<String> jsonError(int status, String message) {
        String esc = message == null ? "" : message.replace("\\", "\\\\").replace("\"", "\\\"");
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":\"" + esc + "\"}");
    }

    @GetMapping("/auth/test")
    public ResponseEntity<String> authTest() {
        return gatewayHttpClient.get(serviceUrls.getAuth() + "/auth/test", null);
    }
}
