package com.yazlab.auth_service.service;

import com.yazlab.auth_service.dto.LoginRequest;
import com.yazlab.auth_service.dto.RegisterRequest;
import com.yazlab.auth_service.model.User;
import com.yazlab.auth_service.client.UserDirectoryClient;
import com.yazlab.auth_service.repository.UserRepository;
import com.yazlab.auth_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDirectoryClient userDirectoryClient;

    public User register(RegisterRequest request) {

        User user = new User(
                request.getUsername(),
                request.getPassword()
        );

        User saved = userRepository.save(user);
        userDirectoryClient.notifyUserCreated(saved.getUsername());
        return saved;
    }

    public String login(LoginRequest request) {

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        String role = "USER";

        if (user.getUsername().equals("admin")) {
            role = "ADMIN";
        }

        return jwtUtil.generateToken(user.getUsername(), role);
    }
}