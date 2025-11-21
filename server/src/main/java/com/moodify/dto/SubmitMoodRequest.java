package com.moodify.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SubmitMoodRequest {
    @NotNull
    @Min(0)
    @Max(5)
    private Integer mood;

    private String reason;

    public Integer getMood() { return mood; }
    public void setMood(Integer mood) { this.mood = mood; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
