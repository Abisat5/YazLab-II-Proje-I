package com.yazlab.auth_service.service;

import com.yazlab.auth_service.dto.RegisterRequest;
import com.yazlab.auth_service.model.User;
import com.yazlab.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.yazlab.auth_service.dto.LoginRequest;
import com.yazlab.auth_service.util.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public User register(RegisterRequest request) {

        User user = new User(
                request.getUsername(),
                request.getPassword()
        );

        return userRepository.save(user);
    }
    public String login(LoginRequest request) {

    User user = userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

    if(!user.getPassword().equals(request.getPassword())){
        throw new RuntimeException("Wrong password");
    }

    return JwtUtil.generateToken(user.getUsername());
}
}