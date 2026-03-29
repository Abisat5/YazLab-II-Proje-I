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
public class ConversationProxyController {

    private final GatewayHttpClient gatewayHttpClient;
    private final ServiceUrlProperties serviceUrls;
    private final ObjectMapper objectMapper;

    public ConversationProxyController(GatewayHttpClient gatewayHttpClient,
                                       ServiceUrlProperties serviceUrls,
                                       ObjectMapper objectMapper) {
        this.gatewayHttpClient = gatewayHttpClient;
        this.serviceUrls = serviceUrls;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/conversations")
    public ResponseEntity<String> createConversation(HttpServletRequest request,
                                                     @RequestBody(required = false) byte[] body) {
        try {
            byte[] json = ProxyBodyNormalizer.toJsonFromBytes(body, objectMapper);
            String xUser = request.getAttribute("username") != null
                    ? request.getAttribute("username").toString()
                    : null;
            ResponseEntity<String> r = gatewayHttpClient.postJson(
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
                                              @PathVariable String conversationId,
                                              @RequestBody(required = false) byte[] body) {
        try {
            byte[] json = ProxyBodyNormalizer.toJsonFromBytes(body, objectMapper);
            String xUser = request.getAttribute("username") != null
                    ? request.getAttribute("username").toString()
                    : null;
            String url = serviceUrls.getMessage() + "/conversations/" + conversationId + "/messages";
            ResponseEntity<String> r = gatewayHttpClient.postJson(url, json, xUser);
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

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<String> deleteConversation(HttpServletRequest request,
                                                       @PathVariable String conversationId) {
        String xUser = request.getAttribute("username") != null
                ? request.getAttribute("username").toString()
                : null;
        String url = serviceUrls.getMessage() + "/conversations/" + conversationId;
        ResponseEntity<String> r = gatewayHttpClient.delete(url, xUser);
        return withJsonContentType(r);
    }

    private static ResponseEntity<String> withJsonContentType(ResponseEntity<String> r) {
        return ResponseEntity.status(r.getStatusCode())
                .header("Content-Type", "application/json")
                .body(r.getBody());
    }

    private ResponseEntity<String> forwardGet(HttpServletRequest request, String url) {
        Object user = request.getAttribute("username");
        String xUser = user != null ? user.toString() : null;
        ResponseEntity<String> response = gatewayHttpClient.get(url, xUser);
        return ResponseEntity.status(response.getStatusCode())
                .header("Content-Type", "application/json")
                .body(response.getBody());
    }
}
