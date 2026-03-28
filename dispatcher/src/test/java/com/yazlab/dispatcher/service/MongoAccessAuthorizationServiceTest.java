package com.yazlab.dispatcher.service;

import com.yazlab.dispatcher.model.AccessRule;
import com.yazlab.dispatcher.repository.AccessRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoAccessAuthorizationServiceTest {

    private AccessRuleRepository repository;
    private MongoAccessAuthorizationService service;

    @BeforeEach
    void setUp() {
        repository = mock(AccessRuleRepository.class);
        service = new MongoAccessAuthorizationService(repository);
    }

    @Test
    void userCannotListAllUsersButAdminCan() {
        when(repository.findByRole("USER")).thenReturn(List.of(
                new AccessRule(null, "USER", "GET", "/profile")));
        when(repository.findByRole("ADMIN")).thenReturn(List.of(
                new AccessRule(null, "ADMIN", "GET", "/users")));

        assertThat(service.canAccess("USER", "GET", "/users")).isFalse();
        assertThat(service.canAccess("ADMIN", "GET", "/users")).isTrue();
    }

    @Test
    void adminInheritsUserConversationPaths() {
        when(repository.findByRole("USER")).thenReturn(List.of(
                new AccessRule(null, "USER", "GET", "/conversations/**")));
        when(repository.findByRole("ADMIN")).thenReturn(List.of(
                new AccessRule(null, "ADMIN", "GET", "/users")));

        assertThat(service.canAccess("ADMIN", "GET", "/conversations/x")).isTrue();
    }
}
