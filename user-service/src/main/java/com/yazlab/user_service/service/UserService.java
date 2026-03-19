package com.yazlab.user_service.service;

import com.yazlab.user_service.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final List<User> users = new ArrayList<>();

    public UserService() {
        // Mock kullanıcı ekle
        users.add(new User("alp", "1234"));
        users.add(new User("ayse", "abcd"));
    }

    public List<User> getAllUsers() {
        return users;
    }
}