package com.yazlab.dispatcher.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class GatewayHttpClient {

    public static final String INTERNAL_TOKEN_HEADER = "X-Yazlab-Internal-Token";

    private final String internalToken;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public GatewayHttpClient(@Value("${yazlab.internal-gateway-token}") String internalToken) {
        this.internalToken = internalToken;
    }

    public ResponseEntity<String> postJson(String url, byte[] body, String xUser) {
        byte[] b = body != null ? body : new byte[0];
        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofByteArray(b))
                .header("Content-Type", "application/json")
                .header(INTERNAL_TOKEN_HEADER, internalToken);
        if (xUser != null && !xUser.isBlank()) {
            rb.header("X-User", xUser);
        }
        return send(rb);
    }

    public ResponseEntity<String> get(String url, String xUser) {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .header(INTERNAL_TOKEN_HEADER, internalToken);
        if (xUser != null && !xUser.isBlank()) {
            rb.header("X-User", xUser);
        }
        return send(rb);
    }

    public ResponseEntity<String> delete(String url, String xUser) {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .DELETE()
                .header(INTERNAL_TOKEN_HEADER, internalToken);
        if (xUser != null && !xUser.isBlank()) {
            rb.header("X-User", xUser);
        }
        return send(rb);
    }

    private ResponseEntity<String> send(HttpRequest.Builder rb) {
        try {
            HttpResponse<String> resp = http.send(rb.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return ResponseEntity.status(resp.statusCode()).body(resp.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body("{\"error\":\"Istek kesildi\"}");
        } catch (Exception e) {
            return ResponseEntity.status(502).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
