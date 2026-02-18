package com.moodify.dto;

import java.util.List;

public class RecommendationResponse {
    private int score;
    private String category; 
    private List<String> activities;
    private String tips;
    private String promptVersion; 
    private boolean cached;

    public RecommendationResponse() {}

    public RecommendationResponse(int score, String category, List<String> activities, String tips, String promptVersion, boolean cached) {
        this.score = score;
        this.category = category;
        this.activities = activities;
        this.tips = tips;
        this.promptVersion = promptVersion;
        this.cached = cached;
    }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getActivities() { return activities; }
    public void setActivities(List<String> activities) { this.activities = activities; }

    public String getTips() { return tips; }
    public void setTips(String tips) { this.tips = tips; }

    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }

    public boolean isCached() { return cached; }
    public void setCached(boolean cached) { this.cached = cached; }
}
