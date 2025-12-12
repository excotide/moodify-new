package com.moodify.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_mood_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "entry_date"})
})
public class DailyMoodEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "entry_date", nullable = false)
    private LocalDate date;

    // Nullable: not filled yet
    private Integer mood;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "week_number")
    private Integer weekNumber;

    @Column(name = "day_name")
    private String dayName;

    @Column(name = "reason")
    private String reason;

    @Column(name = "ai_comment")
    private String aiComment;

    public DailyMoodEntry() {
    }

    public DailyMoodEntry(User user, LocalDate date) {
        this.user = user;
        this.date = date;
    }

    public DailyMoodEntry(User user, LocalDate date, Integer mood, OffsetDateTime createdAt) {
        this.user = user;
        this.date = date;
        this.mood = mood;
        this.createdAt = createdAt;
    }

    public DailyMoodEntry(User user, LocalDate date, Integer weekNumber) {
        this.user = user;
        this.date = date;
        this.weekNumber = weekNumber;
        this.dayName = date.getDayOfWeek().toString();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAiComment() {
        return aiComment;
    }

    public void setAiComment(String aiComment) {
        this.aiComment = aiComment;
    }
}
