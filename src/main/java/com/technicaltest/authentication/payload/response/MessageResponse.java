package com.technicaltest.authentication.payload.response;

import java.io.Serializable;

public class MessageResponse implements Serializable {
    public static final String ACCOUNT_SUSPENDED = "This account is suspended.";
    public static final String USERNAME_EXISTS = "Error: Username already exists.";
    public static final String MAIL_EXISTS = "Error: Email already exists.";
    public static final String ROLE_NOT_FOUND = "Error: Role is not found.";
    public static final String USER_CREATION_SUCCESS = "User registered successfully.";
    public static final String USER_ROLE_UPDATE_SUCCESS = "User roles have been updated successfully.";
    public static final String USER_NOT_FOUND = "User was not recognized.";
    public static final String CAN_NOT_DELETE_OWN_ACCOUNT = "You can not delete your own account.";
    public static final String CAN_NOT_REVOKE_OWN_AUTHORITY = "You can not revoke your own grants.";
    public static final String USER_DELETION_SUCCESS = "User deleted successfully.";
    public static final String PASSWORD_CHANGE_SUCCESS = "Password changed successfully.";

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
