package com.yazlab.dispatcher.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public final class ErrorResponseBody {

    private ErrorResponseBody() {
    }

    public static Map<String, Object> create(int status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("error", message);
        error.put("status", status);
        return error;
    }
}
