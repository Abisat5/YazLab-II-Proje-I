package com.yazlab.message_service.service;

import com.yazlab.message_service.dto.CreateConversationRequest;
import com.yazlab.message_service.dto.SendMessageRequest;
import com.yazlab.message_service.model.ChatMessage;
import com.yazlab.message_service.model.Conversation;
import com.yazlab.message_service.repository.ChatMessageRepository;
import com.yazlab.message_service.repository.ConversationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ConversationService(ConversationRepository conversationRepository,
                              ChatMessageRepository chatMessageRepository) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public Conversation createOrGet(String currentUser, CreateConversationRequest request) {
        String other = request.getParticipantUsername();
        if (other == null || other.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "participantUsername gerekli");
        }
        if (other.equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kendinizle sohbet olusturulamaz");
        }
        var existing = conversationRepository.findConversationBetween(currentUser, other);
        if (existing.isPresent()) {
            return existing.get();
        }
        List<String> pair = new ArrayList<>(List.of(currentUser, other));
        pair.sort(Comparator.naturalOrder());
        Conversation c = new Conversation(pair);
        return conversationRepository.save(c);
    }

    public List<Conversation> listFor(String currentUser) {
        return conversationRepository.findByParticipantsContaining(currentUser);
    }

    public Conversation requireParticipant(String conversationId, String username) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Konusma yok"));
        if (!c.getParticipants().contains(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu konusmaya erisim yok");
        }
        return c;
    }

    public ChatMessage sendMessage(String conversationId, String sender, SendMessageRequest body) {
        if (body.getText() == null || body.getText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mesaj bos olamaz");
        }
        requireParticipant(conversationId, sender);
        ChatMessage m = new ChatMessage(conversationId, sender, body.getText().trim());
        return chatMessageRepository.save(m);
    }

    public List<ChatMessage> listMessages(String conversationId, String viewer) {
        requireParticipant(conversationId, viewer);
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }
}
