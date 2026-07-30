package com.spendsense.user.dto;

import java.util.UUID;

public class UserProfileResponse {

    private UUID userId;
    private String name;
    private String email;
    private String mobileNumber;

    public UserProfileResponse(UUID userId, String name, String email, String mobileNumber) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }
}
