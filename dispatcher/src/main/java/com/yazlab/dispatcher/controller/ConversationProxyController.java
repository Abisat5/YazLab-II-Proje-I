package com.yazlab.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yazlab.dispatcher.config.ServiceUrlProperties;
import com.yazlab.dispatcher.http.HttpForwardClient;
import com.yazlab.dispatcher.http.ProxyBodyNormalizer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RestController
public class ConversationProxyController {

    private final RestTemplate restTemplate;
    private final ServiceUrlProperties serviceUrls;
    private final ObjectMapper objectMapper;

    public ConversationProxyController(RestTemplate restTemplate,
                                       ServiceUrlProperties serviceUrls,
                                       ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.serviceUrls = serviceUrls;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/conversations")
    public ResponseEntity<String> createConversation(HttpServletRequest request) {
        try {
            byte[] json = ProxyBodyNormalizer.toJson(request, objectMapper);
            String xUser = request.getAttribute("username") != null
                    ? request.getAttribute("username").toString()
                    : null;
            ResponseEntity<String> r = HttpForwardClient.postJson(
                    serviceUrls.getMessage() + "/conversations", json, xUser);
            return withJsonContentType(r);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<String> listConversations(HttpServletRequest request) {
        return forwardGet(request, serviceUrls.getMessage() + "/conversations");
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<String> sendMessage(HttpServletRequest request,
                                              @PathVariable String conversationId) {
        try {
            byte[] json = ProxyBodyNormalizer.toJson(request, objectMapper);
            String xUser = request.getAttribute("username") != null
                    ? request.getAttribute("username").toString()
                    : null;
            String url = serviceUrls.getMessage() + "/conversations/" + conversationId + "/messages";
            ResponseEntity<String> r = HttpForwardClient.postJson(url, json, xUser);
            return withJsonContentType(r);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<String> listMessages(HttpServletRequest request,
                                               @PathVariable String conversationId) {
        String url = serviceUrls.getMessage() + "/conversations/" + conversationId + "/messages";
        return forwardGet(request, url);
    }

    private static ResponseEntity<String> withJsonContentType(ResponseEntity<String> r) {
        return ResponseEntity.status(r.getStatusCode())
                .header("Content-Type", "application/json")
                .body(r.getBody());
    }

    private ResponseEntity<String> forwardGet(HttpServletRequest request, String url) {
        HttpHeaders headers = new HttpHeaders();
        Object user = request.getAttribute("username");
        if (user != null) {
            headers.set("X-User", user.toString());
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return ResponseEntity.status(response.getStatusCode())
                .header("Content-Type", "application/json")
                .body(response.getBody());
    }
}
