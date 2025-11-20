package com.moodify.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class WeekRecommendationRequest {
    @NotNull
    private UUID userId;

    @NotNull
    @Min(1)
    @Max(53)
    private Integer weekNumber;

    @Size(max = 500)
    private String context;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}
