package com.moodify.dto;

import java.util.List;

public class WeeklyStatsResponse {
    public static class CategoryShare {
        private final String category;
        private final double percent;

        public CategoryShare(String category, double percent) {
            this.category = category;
            this.percent = percent;
        }
        public String getCategory() { return category; }
        public double getPercent() { return percent; }
    }

    private final Integer weekNumber;
    private final boolean completed; // true jika semua 7 hari minggu itu sudah terisi mood
    private final Double averageScore; // null jika belum lengkap
    private final int entriesCount; // jumlah entri mood (bukan placeholder) dalam minggu
    private final List<CategoryShare> breakdown; // kosong jika belum lengkap
    private final String aiComment; // null jika belum lengkap
    private final List<String> activities; // kosong jika belum lengkap

    public WeeklyStatsResponse(Integer weekNumber, boolean completed, Double averageScore, int entriesCount,
                               List<CategoryShare> breakdown, String aiComment, List<String> activities) {
        this.weekNumber = weekNumber;
        this.completed = completed;
        this.averageScore = averageScore;
        this.entriesCount = entriesCount;
        this.breakdown = breakdown;
        this.aiComment = aiComment;
        this.activities = activities;
    }

    public Integer getWeekNumber() { return weekNumber; }
    public boolean isCompleted() { return completed; }
    public Double getAverageScore() { return averageScore; }
    public int getEntriesCount() { return entriesCount; }
    public List<CategoryShare> getBreakdown() { return breakdown; }
    public String getAiComment() { return aiComment; }
    public List<String> getActivities() { return activities; }
}
