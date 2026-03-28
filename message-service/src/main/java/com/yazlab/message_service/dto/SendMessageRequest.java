package com.yazlab.message_service.dto;

public class SendMessageRequest {

    private String text;

    public SendMessageRequest() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
