package com.spendsense.auth.dto;

import java.util.UUID;

public class LoginResponse {

    private final UUID id;
    private final String name;
    private final String email;
    private final String token;

    public LoginResponse(UUID id, String name, String email, String token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.token = token;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getToken(){
        return token;
    }
}
