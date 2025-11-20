package com.moodify.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class PastMoodRequest {

    @NotNull
    private LocalDate date;

    @NotNull
    private Integer mood;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getMood() {
        return mood;
    }

    public void setMood(Integer mood) {
        this.mood = mood;
    }
}
