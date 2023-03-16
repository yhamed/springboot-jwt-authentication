package com.bootstart.authentication.payload.response;

import java.io.Serializable;

public class MessageResponse implements Serializable {
    public static final String ACCOUNT_SUSPENDED = "This account is suspended.";
    public static final String USERNAME_EXISTS = "Error: Username already exists.";
    public static final String MAIL_EXISTS = "Error: Email already exists.";
    public static final String ROLE_NOT_FOUND = "Error: Role is not found.";
    public static final String USER_CREATION_SUCCESS = "User registered successfully.";

    private String message;

    public MessageResponse() {
    }

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
