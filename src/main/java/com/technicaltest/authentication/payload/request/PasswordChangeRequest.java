package com.technicaltest.authentication.payload.request;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

public class PasswordChangeRequest implements Serializable {

    public PasswordChangeRequest() {
    }

    public PasswordChangeRequest(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    @NotBlank
    private Long id;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
