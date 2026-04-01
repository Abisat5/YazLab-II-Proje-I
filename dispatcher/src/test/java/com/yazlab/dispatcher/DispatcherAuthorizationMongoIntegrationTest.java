package com.yazlab.dispatcher;

import com.yazlab.dispatcher.http.GatewayHttpClient;
import com.yazlab.dispatcher.model.AccessRule;
import com.yazlab.dispatcher.repository.AccessRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DispatcherAuthorizationMongoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessRuleRepository accessRuleRepository;

    @MockitoBean
    private GatewayHttpClient gatewayHttpClient;

    @BeforeEach
    void setUp() {
        accessRuleRepository.deleteAll();
        accessRuleRepository.saveAll(List.of(
                new AccessRule(null, "USER", "GET", "/profile"),
                new AccessRule(null, "ADMIN", "GET", "/users")
        ));
    }

    @Test
    void usersEndpoint_AdminAllowed_RulesComeFromMongo() throws Exception {
        String expectedBody = "[{\"username\":\"alice\"}]";
        Mockito.when(gatewayHttpClient.get(eq("http://user-service:8082/users"), eq("admin")))
                .thenReturn(new ResponseEntity<>(expectedBody, HttpStatus.OK));

        String token = JwtTestUtil.generateToken("admin", "ADMIN");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedBody));
    }

    @Test
    void usersEndpoint_UserForbidden_RulesComeFromMongo() throws Exception {
        Mockito.when(gatewayHttpClient.get(anyString(), anyString()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        String token = JwtTestUtil.generateToken("bob", "USER");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(content().string("{\"error\":\"Bu kaynak icin yetkiniz yok\"}"));
    }
}

