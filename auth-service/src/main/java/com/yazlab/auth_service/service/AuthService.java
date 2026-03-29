package com.yazlab.auth_service.service;

import com.yazlab.auth_service.dto.LoginRequest;
import com.yazlab.auth_service.dto.RegisterRequest;
import com.yazlab.auth_service.model.User;
import com.yazlab.auth_service.client.UserDirectoryClient;
import com.yazlab.auth_service.repository.UserRepository;
import com.yazlab.auth_service.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDirectoryClient userDirectoryClient;

    public User register(RegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        if (username.isEmpty()) {
            throw new RuntimeException("Username required");
        }
        if (userRepository.findFirstByUsername(username).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User(username, request.getPassword() != null ? request.getPassword() : "");

        User saved = userRepository.save(user);
        userDirectoryClient.notifyUserCreated(saved.getUsername());
        return saved;
    }

    public String login(LoginRequest request) {
        String username = normalizeUsername(request.getUsername());
        if (username.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userRepository
                .findFirstByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String reqPwd = request.getPassword() != null ? request.getPassword() : "";
        if (!user.getPassword().equals(reqPwd)) {
            throw new RuntimeException("Wrong password");
        }

        String role = "USER";
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            role = "ADMIN";
        }

        syncUserDirectory(user.getUsername());

        return jwtUtil.generateToken(user.getUsername(), role);
    }

    /** Auth'ta var ama user-service'te yoksa  profil olusturur; idempotent. */
    private void syncUserDirectory(String username) {
        try {
            userDirectoryClient.notifyUserCreated(username);
        } catch (Exception e) {
            log.warn("user-service profil eşlemesi yapılamadı: {}", e.getMessage());
        }
    }

    private static String normalizeUsername(String raw) {
        return raw == null ? "" : raw.trim();
    }
}