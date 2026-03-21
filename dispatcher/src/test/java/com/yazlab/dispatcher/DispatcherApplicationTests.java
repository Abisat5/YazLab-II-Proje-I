package com.yazlab.dispatcher;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DispatcherApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MockMvc mockMvc; // API isteklerini simüle etmek için

    @Test
    void testLoggingFilterExists() {
        assertThat(context.containsBean("loggingFilter")).isTrue();
    }

    // TDD kuralı gereği asıl kodu düzeltmeden önce yazdığımız test
    @Test
    void tokenExceptionMustBe401() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }



    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void usersEndpoint_ShouldRouteToUserService() throws Exception {
        // User Service'in (8082) döneceği sahte bir cevap hazırlıyoruz
        String expectedResponseFromUserService = "[{\"username\":\"gercek_kullanici\"}]";

        // Dispatcher içindeki RestTemplate'in User Service'e gitmesini simüle ediyoruz
        Mockito.when(restTemplate.getForEntity("http://localhost:8082/users", String.class))
                .thenReturn(new ResponseEntity<>(expectedResponseFromUserService, HttpStatus.OK));

        // Dispatcher'a /users isteği atıyoruz (Geçerli bir token ile)
        mockMvc.perform(get("/users").header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(expectedResponseFromUserService));
    }
}