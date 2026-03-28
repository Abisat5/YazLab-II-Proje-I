package com.yazlab.message_service.dto;

public class CreateConversationRequest {

    private String participantUsername;

    public CreateConversationRequest() {
    }

    public String getParticipantUsername() {
        return participantUsername;
    }

    public void setParticipantUsername(String participantUsername) {
        this.participantUsername = participantUsername;
    }
}
