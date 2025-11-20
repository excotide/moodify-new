package com.moodify.controller;

import com.moodify.entity.User;
import com.moodify.dto.PastMoodRequest;
import com.moodify.service.DailyMoodService;
import com.moodify.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/mood-entries")
public class MoodEntryController {

    @Autowired
    private UserService userService;

    @Autowired
    private DailyMoodService dailyMoodService;

    @PostMapping("/users/{id}/mood")
    public ResponseEntity<?> submitMoodToday(@PathVariable UUID id, @RequestBody Map<String, Integer> body) {
        User u = userService.getById(id);
        Integer mood = body.get("mood");
        if (mood == null) {
            return ResponseEntity.badRequest().body("mood is required");
        }
        try {
            dailyMoodService.submitTodayMood(u, mood);
            return ResponseEntity.ok().build();
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
            dailyMoodService.submitPastMood(u, req.getDate(), req.getMood());
            var history = dailyMoodService.getHistoryFromFirstToLastLogin(u).stream()
                    .map(e -> new com.moodify.dto.DailyMoodResponse(
                            e.getDate(), e.getDayName(), e.getWeekNumber(), e.getMood(), e.getCreatedAt()
                    ))
                    .toList();
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}