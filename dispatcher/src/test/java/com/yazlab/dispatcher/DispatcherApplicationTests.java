package com.yazlab.dispatcher;

import com.yazlab.dispatcher.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DispatcherApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void testLoggingFilterExists() {
        assertThat(context.containsBean("loggingFilter")).isTrue();
    }

    @Test
    void tokenExceptionMustBe401() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usersEndpoint_ShouldRouteToUserService() throws Exception {

        String expectedResponse = "[{\"username\":\"gercek_kullanici\"}]";

        // JWT mock
        Mockito.when(jwtUtil.validateToken(anyString())).thenReturn(true);
        Mockito.when(jwtUtil.extractUsername(anyString())).thenReturn("admin");
        Mockito.when(jwtUtil.extractRole(anyString())).thenReturn("ADMIN");

        // User-service mock
        Mockito.when(restTemplate.exchange(
                        eq("http://user-service:8082/users"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(String.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer valid.jwt.stub"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void usersEndpoint_ShouldReturn403_ForNonAdmin() throws Exception {

        Mockito.when(jwtUtil.validateToken(anyString())).thenReturn(true);
        Mockito.when(jwtUtil.extractUsername(anyString())).thenReturn("user");
        Mockito.when(jwtUtil.extractRole(anyString())).thenReturn("USER");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer valid.jwt.stub"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("{\"error\":\"Bu işlem için yetkiniz yok\"}"));
    }

    @Test
    void profileEndpoint_ShouldRouteToUserServiceMe() throws Exception {

        String body = "{\"id\":\"1\",\"username\":\"alice\"}";

        Mockito.when(jwtUtil.validateToken(anyString())).thenReturn(true);
        Mockito.when(jwtUtil.extractUsername(anyString())).thenReturn("alice");
        Mockito.when(jwtUtil.extractRole(anyString())).thenReturn("USER");

        Mockito.when(restTemplate.exchange(
                        eq("http://user-service:8082/users/me"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(String.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        mockMvc.perform(get("/profile")
                        .header("Authorization", "Bearer valid.jwt.stub"))
                .andExpect(status().isOk())
                .andExpect(content().string(body));
    }
}