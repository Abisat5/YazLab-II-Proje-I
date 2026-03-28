package com.yazlab.message_service.repository;

import com.yazlab.message_service.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    List<Conversation> findByParticipantsContaining(String username);

    @Query("{ 'participants': { $all: [?0, ?1], $size: 2 } }")
    Optional<Conversation> findConversationBetween(String userOne, String userTwo);
}
