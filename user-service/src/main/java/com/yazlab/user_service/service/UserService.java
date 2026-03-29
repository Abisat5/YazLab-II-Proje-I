package com.yazlab.user_service.service;

import com.yazlab.user_service.model.User;
import com.yazlab.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByUsername(String username) {
        return userRepository.findFirstByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Ilk profil isteginde bos sifreli kayit olusturur.
     */
    public User getOrCreateProfile(String username) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Kullanıcı bilgisi yok");
        }
        return userRepository.findFirstByUsername(username)
                .orElseGet(() -> userRepository.save(new User(username, "")));
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}