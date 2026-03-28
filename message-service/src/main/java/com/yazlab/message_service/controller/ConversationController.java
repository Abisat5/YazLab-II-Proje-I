package com.yazlab.message_service.controller;

import com.yazlab.message_service.dto.CreateConversationRequest;
import com.yazlab.message_service.dto.SendMessageRequest;
import com.yazlab.message_service.model.ChatMessage;
import com.yazlab.message_service.model.Conversation;
import com.yazlab.message_service.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<Conversation> create(@RequestHeader("X-User") String username,
                                              @RequestBody CreateConversationRequest body) {
        Conversation c = conversationService.createOrGet(username, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    @GetMapping
    public List<Conversation> list(@RequestHeader("X-User") String username) {
        return conversationService.listFor(username);
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<ChatMessage> send(@RequestHeader("X-User") String username,
                                            @PathVariable String conversationId,
                                            @RequestBody SendMessageRequest body) {
        ChatMessage saved = conversationService.sendMessage(conversationId, username, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{conversationId}/messages")
    public List<ChatMessage> messages(@RequestHeader("X-User") String username,
                                        @PathVariable String conversationId) {
        return conversationService.listMessages(conversationId, username);
    }
}
