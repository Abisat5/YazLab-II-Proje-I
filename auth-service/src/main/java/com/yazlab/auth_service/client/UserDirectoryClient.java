package com.yazlab.auth_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class UserDirectoryClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${yazlab.user-service.base-url:http://localhost:8082}")
    private String userServiceBaseUrl;

    @Value("${yazlab.internal-gateway-token}")
    private String internalToken;

    public void notifyUserCreated(String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Yazlab-Internal-Token", internalToken);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("username", username), headers);
        restTemplate.postForEntity(userServiceBaseUrl + "/internal/profiles", entity, Void.class);
    }
}
