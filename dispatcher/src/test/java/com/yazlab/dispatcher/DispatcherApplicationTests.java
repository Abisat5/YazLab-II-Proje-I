package com.yazlab.dispatcher;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DispatcherApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testDispatcherUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("/test", String.class);
        assertEquals("Dispatcher calisiyor sikinti yok", response.getBody());
    }
}