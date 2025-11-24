package com.moodify.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodify.ai.OpenAIClient;
import com.moodify.dto.WeekRecommendationRequest;
import com.moodify.dto.WeeklyStatsResponse;
import com.moodify.entity.DailyMoodEntry;
import com.moodify.entity.User;
import com.moodify.entity.WeeklyStats;
import com.moodify.repository.WeeklyStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeeklyStatsService {

    private final WeeklyStatsRepository weeklyStatsRepository;
    private final DailyMoodService dailyMoodService;
    private final UserService userService;
    private final OpenAIClient openAIClient;
    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    public WeeklyStatsService(WeeklyStatsRepository weeklyStatsRepository,
                              DailyMoodService dailyMoodService,
                              UserService userService,
                              OpenAIClient openAIClient,
                              RecommendationService recommendationService,
                              ObjectMapper objectMapper) {
        this.weeklyStatsRepository = weeklyStatsRepository;
        this.dailyMoodService = dailyMoodService;
        this.userService = userService;
        this.openAIClient = openAIClient;
        this.recommendationService = recommendationService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public WeeklyStatsResponse getOrCreateWeeklyStats(UUID userId, Integer weekNumberOpt) {
        User user = userService.getById(userId);
        Integer weekNumber = weekNumberOpt;
        if (weekNumber == null) {
            var currentWeek = dailyMoodService.getCurrentWeek(user);
            weekNumber = currentWeek.stream().findFirst().map(DailyMoodEntry::getWeekNumber).orElse(1);
        }

        final Integer targetWeek = weekNumber;
        Optional<WeeklyStats> existing = weeklyStatsRepository.findByUserAndWeekNumber(user, targetWeek);

        // Ambil entri minggu spesifik langsung agar tidak tergantung lastLogin range
        var weekEntries = dailyMoodService.getWeek(user, targetWeek);

        String fingerprint = buildFingerprint(user, targetWeek, weekEntries);
        if (existing.isPresent()) {
            WeeklyStats wsExisting = existing.get();
            String prev = wsExisting.getFingerprint();
            if (prev == null || prev.isBlank()) {
                // Rekam fingerprint pertama kali tanpa mengubah isi stats lama
                wsExisting.setFingerprint(fingerprint);
                weeklyStatsRepository.save(wsExisting);
                return toDto(wsExisting);
            }
            if (fingerprint.equals(prev)) {
                return toDto(wsExisting);
            }
        }

        int filledCount = (int) weekEntries.stream().filter(e -> e.getMood() != null).count();
        boolean completeWeek = weekEntries.size() == 7 && filledCount == 7;
        Double average = null;
        if (completeWeek) {
            average = weekEntries.stream()
                .mapToInt(DailyMoodEntry::getMood)
                .average().orElse(0.0);
            if (average < 0) average = 0.0; if (average > 5) average = 5.0;
        }

        Map<String, Integer> counts = new HashMap<>();
        List<WeeklyStatsResponse.CategoryShare> breakdown = new ArrayList<>();
        if (completeWeek) {
            weekEntries.forEach(e -> {
                String cat = toCategory(e.getMood());
                counts.put(cat, counts.getOrDefault(cat, 0) + 1);
            });
            for (var en : counts.entrySet()) {
                double percent = (en.getValue() * 100.0) / 7.0;
                breakdown.add(new WeeklyStatsResponse.CategoryShare(en.getKey(), percent));
            }
            breakdown.sort(Comparator.comparingDouble(WeeklyStatsResponse.CategoryShare::getPercent).reversed());
        }

        String breakdownText = completeWeek
            ? breakdown.stream()
                .map(cs -> String.format(java.util.Locale.US, "%.1f%% %s", cs.getPercent(), capitalize(cs.getCategory())))
                .collect(Collectors.joining(", "))
            : "Minggu belum lengkap";

        String aiComment = null;
        List<String> activities = List.of();
        if (completeWeek) {
            String profile = buildUserProfileContext(user);
            aiComment = openAIClient.weeklySummaryComment(breakdownText, profile).orElse(
                breakdownText + " — Tetap jaga kesehatan emosimu."
            );
            WeekRecommendationRequest wreq = new WeekRecommendationRequest();
            wreq.setUserId(userId);
            wreq.setWeekNumber(targetWeek);
            var rec = recommendationService.recommendForWeek(wreq);
            activities = rec.getActivities();
        }

        if (!completeWeek) {
            // Jangan persist jika belum lengkap. Jika ada existing & fingerprint beda kita tidak overwrite.
            if (existing.isPresent()) {
                return toDto(existing.get());
            }
            return new WeeklyStatsResponse(targetWeek, false, null, filledCount, List.of(), null, List.of());
        }

        // Persist hanya saat minggu lengkap atau belum pernah disimpan.
        WeeklyStats ws = existing.orElseGet(WeeklyStats::new);
        ws.setUser(user);
        ws.setWeekNumber(targetWeek);
        ws.setAverageScore(average);
        ws.setEntriesCount(filledCount);
        try {
            ws.setBreakdownJson(objectMapper.writeValueAsString(breakdown));
            ws.setActivitiesJson(objectMapper.writeValueAsString(activities));
        } catch (Exception e) {
            ws.setBreakdownJson("[]");
            ws.setActivitiesJson("[]");
        }
        ws.setAiComment(aiComment);
        ws.setFingerprint(fingerprint);
        weeklyStatsRepository.save(ws);

        return new WeeklyStatsResponse(targetWeek, true, average, filledCount, breakdown, aiComment, activities);
    }

    private WeeklyStatsResponse toDto(WeeklyStats ws) {
        List<WeeklyStatsResponse.CategoryShare> breakdown = new ArrayList<>();
        List<String> activities = List.of();
        try {
            if (ws.getBreakdownJson() != null && !ws.getBreakdownJson().isBlank()) {
                var arr = objectMapper.readValue(ws.getBreakdownJson(), new TypeReference<List<Map<String,Object>>>(){});
                for (var m : arr) {
                    String cat = Objects.toString(m.get("category"), null);
                    double pct = 0.0;
                    Object p = m.get("percent");
                    if (p instanceof Number n) pct = n.doubleValue();
                    else if (p != null) pct = Double.parseDouble(p.toString());
                    breakdown.add(new WeeklyStatsResponse.CategoryShare(cat, pct));
                }
            }
        } catch (Exception ignore) {}
        try {
            if (ws.getActivitiesJson() != null && !ws.getActivitiesJson().isBlank()) {
                activities = objectMapper.readValue(ws.getActivitiesJson(), new TypeReference<List<String>>(){});
            }
        } catch (Exception ignore) {}
        boolean completed = ws.getEntriesCount() != null && ws.getEntriesCount() == 7 && ws.getAverageScore() != null;
        return new WeeklyStatsResponse(ws.getWeekNumber(), completed, ws.getAverageScore(),
            ws.getEntriesCount() == null ? 0 : ws.getEntriesCount(),
            completed ? breakdown : List.of(), completed ? ws.getAiComment() : null, completed ? activities : List.of());
    }

    private String toCategory(int score) {
        return switch (score) {
            case 1 -> "angry";
            case 2 -> "sad";
            case 3 -> "neutral";
            case 4 -> "happy";
            default -> "joy";
        };
    }

    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private String buildFingerprint(User user, Integer weekNumber, List<DailyMoodEntry> weekEntries) {
        // Build canonical week range (7 days) based on user's anchor and weekNumber
        java.time.LocalDate anchor = anchorOf(user);
        java.time.LocalDate start = anchor.plusDays((long) (weekNumber - 1) * 7);
        Map<java.time.LocalDate, Integer> moodByDate = weekEntries.stream()
                .filter(e -> e.getDate() != null)
                .collect(Collectors.toMap(DailyMoodEntry::getDate, DailyMoodEntry::getMood, (a,b) -> a));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            var d = start.plusDays(i);
            Integer m = moodByDate.get(d);
            if (i > 0) sb.append('|');
            sb.append(d.toString()).append(':').append(m == null ? '_' : m);
        }
        return sb.toString();
    }

    private java.time.LocalDate anchorOf(User user) {
        if (user.getFirstLogin() != null) return user.getFirstLogin().toLocalDate();
        if (user.getCreatedAt() != null) return user.getCreatedAt().toLocalDate();
        return java.time.LocalDate.now();
    }

    private String buildUserProfileContext(User u) {
        StringBuilder sb = new StringBuilder();
        if (u.getBirthDate() != null) {
            try {
                int age = java.time.Period.between(u.getBirthDate(), java.time.LocalDate.now()).getYears();
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
                java.util.List<String> hobbies = objectMapper.readValue(hj, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>(){});
                if (hobbies != null && !hobbies.isEmpty()) {
                    if (sb.length() > 0) sb.append(" | ");
                    java.util.List<String> trimmed = hobbies.size() > 6 ? hobbies.subList(0, 6) : hobbies;
                    sb.append("hobi:").append(String.join(",", trimmed));
                }
            } catch (Exception ignored) {
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
