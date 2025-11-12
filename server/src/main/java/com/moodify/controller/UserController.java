package com.moodify.controller;

import com.moodify.dto.UserRegisterRequest;
import com.moodify.dto.UserResponse;
import com.moodify.entity.User;
import com.moodify.service.UserService;
import com.moodify.service.DailyMoodService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DailyMoodService dailyMoodService;

    @GetMapping("/{id}/week")
    public java.util.List<com.moodify.dto.DailyMoodResponse> getUpcomingWeek(@PathVariable UUID id) {
        User u = userService.getById(id);
        var entries = dailyMoodService.getUpcomingWeek(u);
        return entries.stream().map(e -> new com.moodify.dto.DailyMoodResponse(
                e.getDate(), e.getDayName(), e.getWeekNumber(), e.getMood(), e.getCreatedAt()
        )).toList();
    }

    @PostMapping({"", "/register"})
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest req) {
        User saved = userService.register(req);
        UserResponse resp = new UserResponse(saved.getId(), saved.getUsername(), saved.getCreatedAt());
        return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        User u = userService.getById(id);
        UserResponse resp = new UserResponse(u.getId(), u.getUsername(), u.getCreatedAt());
        return ResponseEntity.ok(resp);
    }

    // Inisialisasi week mood entries bila user belum punya entri (dipakai saat first login)
    @PostMapping("/{id}/initialize-week")
    public ResponseEntity<Void> initializeWeek(@PathVariable UUID id) {
        User u = userService.getById(id);
        dailyMoodService.initializeWeekIfFirstLogin(u);
        return ResponseEntity.ok().build();
    }

    // Submit mood untuk hari ini. Body: {"mood": 4}
    @PostMapping("/{id}/mood")
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
}
