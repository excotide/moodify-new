package com.moodify.controller;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moodify.dto.UserRegisterRequest;
import com.moodify.dto.UserResponse;
import com.moodify.dto.WeeklyStatsResponse;
import com.moodify.entity.User;
import com.moodify.service.DailyMoodService;
import com.moodify.service.UserService;
import com.moodify.service.WeeklyStatsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DailyMoodService dailyMoodService;

    @Autowired
    private WeeklyStatsService weeklyStatsService;

    @GetMapping("/{id}/week")
    public java.util.List<com.moodify.dto.DailyMoodResponse> getWeek(@PathVariable UUID id,
                                                                     @RequestParam(name = "weekNumber", required = false) Integer weekNumber) {
        User u = userService.getById(id);
        var entries = (weekNumber != null) ? dailyMoodService.getWeek(u, weekNumber) : dailyMoodService.getUpcomingWeek(u);
        return entries.stream().map(e -> new com.moodify.dto.DailyMoodResponse(
                e.getDate(), e.getDayName(), e.getWeekNumber(), e.getMood(), e.getCreatedAt(),
                e.getReason(), e.getAiComment()
        )).toList();
    }

    @GetMapping("/{id}/stats")
    public WeeklyStatsResponse getWeeklyStats(@PathVariable UUID id, @RequestParam(name = "weekNumber", required = false) Integer weekNumber) {
        return weeklyStatsService.getOrCreateWeeklyStats(id, weekNumber);
    }

    @GetMapping("/{id}/moods/history")
    public java.util.List<com.moodify.dto.DailyMoodResponse> getHistoryFromFirstToLastLogin(@PathVariable UUID id) {
        User u = userService.getById(id);
        var entries = dailyMoodService.getHistoryFromFirstToLastLogin(u);
        return entries.stream()
            .sorted(java.util.Comparator.comparing(com.moodify.entity.DailyMoodEntry::getDate))
            .map(e -> new com.moodify.dto.DailyMoodResponse(
                e.getDate(), e.getDayName(), e.getWeekNumber(), e.getMood(), e.getCreatedAt(),
                e.getReason(), e.getAiComment()
            ))
            .toList();
    }

    @PostMapping({"", "/register"})
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest req) {
        User saved = userService.register(req);
        UserResponse resp = new UserResponse(saved.getId(), saved.getUsername(), saved.getCreatedAt());
        URI location = Objects.requireNonNull(URI.create("/api/users/" + saved.getId()));
        return ResponseEntity.created(location).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        User u = userService.getById(id);
        UserResponse resp = new UserResponse(u.getId(), u.getUsername(), u.getCreatedAt());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}/currentWeek")
    public Map<String, Integer> getCurrentWeekNumber(@PathVariable UUID id) {
        User u = userService.getById(id);
        var weekEntries = dailyMoodService.getCurrentWeek(u);
        int weekNumber = weekEntries.stream()
                .findFirst()
                .map(com.moodify.entity.DailyMoodEntry::getWeekNumber)
                .orElse(1);
        return Map.of("weekNumber", weekNumber);
    }

    // Inisialisasi week mood entries bila user belum punya entri (dipakai saat first login)
    // @PostMapping("/{id}/initialize-week")
    // public ResponseEntity<Void> initializeWeek(@PathVariable UUID id) {
    //     User u = userService.getById(id);
    //     dailyMoodService.initializeWeekIfFirstLogin(u);
    //     return ResponseEntity.ok().build();
    // }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        try {
            String token = userService.login(username, password); // Validate login and update first/last login
            var userId = userService.findUserByUsername(username)
                    .map(User::getId)
                    .orElse(null);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", userId != null ? userId.toString() : null
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
