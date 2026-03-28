package com.yazlab.dispatcher;

import com.yazlab.dispatcher.http.GatewayHttpClient;
import com.yazlab.dispatcher.service.AccessAuthorizationService;
import com.yazlab.dispatcher.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DispatcherApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GatewayHttpClient gatewayHttpClient;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AccessAuthorizationService accessAuthorizationService;

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

        Mockito.when(jwtUtil.validateToken(anyString())).thenReturn(true);
        Mockito.when(jwtUtil.extractUsername(anyString())).thenReturn("admin");
        Mockito.when(jwtUtil.extractRole(anyString())).thenReturn("ADMIN");

        Mockito.when(accessAuthorizationService.canAccess(eq("ADMIN"), eq("GET"), eq("/users")))
                .thenReturn(true);

        Mockito.when(gatewayHttpClient.get(eq("http://localhost:8082/users"), eq("admin")))
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

        Mockito.when(accessAuthorizationService.canAccess(eq("USER"), eq("GET"), eq("/users")))
                .thenReturn(false);

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer valid.jwt.stub"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("{\"error\":\"Bu kaynak icin yetkiniz yok\"}"));
    }

    @Test
    void profileEndpoint_ShouldRouteToUserServiceMe() throws Exception {

        String body = "{\"id\":\"1\",\"username\":\"alice\"}";

        Mockito.when(jwtUtil.validateToken(anyString())).thenReturn(true);
        Mockito.when(jwtUtil.extractUsername(anyString())).thenReturn("alice");
        Mockito.when(jwtUtil.extractRole(anyString())).thenReturn("USER");

        Mockito.when(accessAuthorizationService.canAccess(eq("USER"), eq("GET"), eq("/profile")))
                .thenReturn(true);

        Mockito.when(gatewayHttpClient.get(eq("http://localhost:8082/users/me"), eq("alice")))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        mockMvc.perform(get("/profile")
                        .header("Authorization", "Bearer valid.jwt.stub"))
                .andExpect(status().isOk())
                .andExpect(content().string(body));
    }
}
