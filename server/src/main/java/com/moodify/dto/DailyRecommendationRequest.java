package com.moodify.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class DailyRecommendationRequest {
    private UUID userId; // optional

    @NotNull
    @Min(0)
    @Max(5)
    private Integer score;

    @Size(max = 500)
    private String context;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}
