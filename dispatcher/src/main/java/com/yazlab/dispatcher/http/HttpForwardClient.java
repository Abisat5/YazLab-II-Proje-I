package com.yazlab.dispatcher.http;

import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * RestTemplate ile JSON govdesi bazen bozuluyordu; ham bayt iletmek icin java.net.http.
 */
public final class HttpForwardClient {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private HttpForwardClient() {
    }

    public static ResponseEntity<String> postJson(String url, byte[] body, String xUser) {
        byte[] b = body != null ? body : new byte[0];
        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofByteArray(b))
                .header("Content-Type", "application/json");
        if (xUser != null && !xUser.isBlank()) {
            rb.header("X-User", xUser);
        }
        try {
            HttpResponse<String> resp = HTTP.send(rb.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return ResponseEntity.status(resp.statusCode()).body(resp.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body("{\"error\":\"Istek kesildi\"}");
        } catch (Exception e) {
            return ResponseEntity.status(502).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
