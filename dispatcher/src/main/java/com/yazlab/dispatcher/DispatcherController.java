package com.yazlab.dispatcher;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DispatcherController {

    @GetMapping("/test")
    public String testDispatcher() {
        return "Dispatcher calisiyor sikinti yok";
    }
}