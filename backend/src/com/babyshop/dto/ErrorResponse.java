package com.babyshop.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    public String timestamp;
    public int status;
    public String message;
    public String path;
    public Map<String, String> fieldErrors;

    public ErrorResponse(int status, String message, String path, Map<String, String> fieldErrors) {
        this.timestamp = LocalDateTime.now().toString();
        this.status = status;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }
}
