package com.moodify.controller;

import com.moodify.entity.User;
import com.moodify.dto.PastMoodRequest;
import com.moodify.service.DailyMoodService;
import com.moodify.dto.SubmitMoodRequest;
import com.moodify.ai.OpenAIClient;
import com.moodify.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/mood-entries")
public class MoodEntryController {

    @Autowired
    private UserService userService;

    @Autowired
    private DailyMoodService dailyMoodService;

    @Autowired
    private OpenAIClient openAIClient;

    @PostMapping("/users/{id}/mood")
    public ResponseEntity<?> submitMoodToday(@PathVariable UUID id, @RequestBody SubmitMoodRequest body) {
        User u = userService.getById(id);
        Integer mood = body.getMood();
        if (mood == null) {
            return ResponseEntity.badRequest().body("mood is required");
        }
        try {
            var saved = dailyMoodService.submitTodayMood(u, mood, body.getReason());
            // Generate AI comment (persist inside service method)
            if (body.getReason() != null && !body.getReason().isBlank()) {
                openAIClient.commentOnReason(mood, body.getReason())
                        .ifPresent(c -> dailyMoodService.addAiComment(saved, c));
            }
            var out = new com.moodify.dto.DailyMoodResponse(
                    saved.getDate(), saved.getDayName(), saved.getWeekNumber(), saved.getMood(), saved.getCreatedAt(),
                    saved.getReason(), saved.getAiComment()
            );
            return ResponseEntity.ok(out);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/users/{id}/mood/past")
    public ResponseEntity<?> submitPastMood(
            @PathVariable UUID id,
            @RequestBody PastMoodRequest req) {
        User u = userService.getById(id);
        if (req.getMood() == null || req.getDate() == null) {
            return ResponseEntity.badRequest().body("date and mood are required");
        }
        try {
                var saved = dailyMoodService.submitPastMood(u, req.getDate(), req.getMood(), req.getReason());
                if (req.getReason() != null && !req.getReason().isBlank()) {
                    openAIClient.commentOnReason(req.getMood(), req.getReason())
                        .ifPresent(c -> dailyMoodService.addAiComment(saved, c));
                }
            var history = dailyMoodService.getHistoryFromFirstToLastLogin(u).stream()
                    .map(e -> new com.moodify.dto.DailyMoodResponse(
                        e.getDate(), e.getDayName(), e.getWeekNumber(), e.getMood(), e.getCreatedAt(),
                        e.getReason(), e.getAiComment()
                    ))
                    .toList();
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}