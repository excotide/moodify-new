package com.moodify.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class DailyMoodResponse {
    private LocalDate date;
    private String dayName;
    private Integer weekNumber;
    private Integer mood;
    private OffsetDateTime createdAt;

    public DailyMoodResponse() {}

    public DailyMoodResponse(LocalDate date, String dayName, Integer weekNumber, Integer mood, OffsetDateTime createdAt) {
        this.date = date;
        this.dayName = dayName;
        this.weekNumber = weekNumber;
        this.mood = mood;
        this.createdAt = createdAt;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDayName() {
        return dayName;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public Integer getMood() {
        return mood;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
