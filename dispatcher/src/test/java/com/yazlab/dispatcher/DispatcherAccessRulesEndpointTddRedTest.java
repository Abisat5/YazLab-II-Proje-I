package com.yazlab.dispatcher;

import com.yazlab.dispatcher.model.AccessRule;
import com.yazlab.dispatcher.repository.AccessRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DispatcherAccessRulesEndpointTddRedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessRuleRepository accessRuleRepository;

    @BeforeEach
    void setUp() {
        accessRuleRepository.deleteAll();
        accessRuleRepository.saveAll(List.of(
                new AccessRule(null, "ADMIN", "GET", "/access-rules")
        ));
    }

    @Test
    void adminAccessRulesEndpoint_mustExistAndReturnRules() throws Exception {
        // Bu endpoint henüz implement edilmediği için test (RED) aşamasında fail olmalı.
        String token = JwtTestUtil.generateToken("admin", "ADMIN");

        mockMvc.perform(get("/access-rules")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("access-rules")))
                .andExpect(status().is(HttpStatus.OK.value()));
    
    @Test
void deleteAccessRule_whenAdminTokenProvided_shouldReturnNoContentAndDeleteRule() throws Exception {
    AccessRule rule = accessRuleRepository.save(
            new AccessRule(null, "ADMIN", "POST", "/users")
    );

    String token = JwtTestUtil.generateToken("admin", "ADMIN");

    mockMvc.perform(delete("/access-rules/{id}", rule.getId())
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isNoContent());

    org.junit.jupiter.api.Assertions.assertFalse(
            accessRuleRepository.findById(rule.getId()).isPresent()
    );
}
    }
}

