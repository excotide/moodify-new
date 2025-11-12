package com.moodify.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class MoodEntryResponse {
    private UUID id;
    private UUID userId;
    private String mood;
    private Integer score;
    private OffsetDateTime timestamp;

    public MoodEntryResponse(UUID id, UUID userId, String mood, Integer score, OffsetDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.mood = mood;
        this.score = score;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getMood() {
        return mood;
    }

    public Integer getScore() {
        return score;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}