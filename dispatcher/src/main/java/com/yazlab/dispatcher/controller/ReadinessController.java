package com.yazlab.dispatcher.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReadinessController {

    @GetMapping("/ready")
    public Map<String, String> ready() {
        return Map.of(
                "service", "dispatcher",
                "status", "UP");
    }
}
