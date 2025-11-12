package com.moodify.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserResponse {
    private final UUID id;
    private final String username;
    private final OffsetDateTime createdAt;

    public UserResponse(UUID id, String username, OffsetDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
