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

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

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
                String profile = buildUserProfileContext(u);
                openAIClient.commentOnReason(mood, body.getReason(), profile)
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
                String profile = buildUserProfileContext(u);
                openAIClient.commentOnReason(req.getMood(), req.getReason(), profile)
                        .ifPresent(c -> dailyMoodService.addAiComment(saved, c));
            }
            var out = new com.moodify.dto.DailyMoodResponse(
                    saved.getDate(), saved.getDayName(), saved.getWeekNumber(), saved.getMood(), saved.getCreatedAt(),
                    saved.getReason(), saved.getAiComment()
            );
            return ResponseEntity.ok(out);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    private String buildUserProfileContext(User u) {
        StringBuilder sb = new StringBuilder();
        if (u.getBirthDate() != null) {
            try {
                int age = Period.between(u.getBirthDate(), LocalDate.now()).getYears();
                if (age > 0 && age < 120) {
                    if (sb.length() > 0) sb.append(" | ");
                    sb.append("umur:").append(age);
                }
            } catch (Exception ignored) {}
        }
        if (u.getGender() != null && !u.getGender().isBlank()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("gender:").append(u.getGender());
        }
        String hj = u.getHobbiesJson();
        if (hj != null && !hj.isBlank()) {
            try {
                List<String> hobbies = new ObjectMapper().readValue(hj, new TypeReference<List<String>>(){});
                if (hobbies != null && !hobbies.isEmpty()) {
                    if (sb.length() > 0) sb.append(" | ");
                    List<String> trimmed = hobbies.size() > 6 ? hobbies.subList(0, 6) : hobbies;
                    sb.append("hobi:").append(String.join(",", trimmed));
                }
            } catch (Exception ignored) {
                // fallback: include raw string but keep short
                String raw = hj.trim();
                if (!raw.isEmpty()) {
                    if (sb.length() > 0) sb.append(" | ");
                    sb.append("hobi:").append(raw.length() > 120 ? raw.substring(0, 120) : raw);
                }
            }
        }
        String out = sb.toString();
        return out.length() > 400 ? out.substring(0, 400) : out;
    }
}