package com.moodify.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodify.ai.OpenAIClient;
import com.moodify.dto.DailyRecommendationRequest;
import com.moodify.dto.RecommendationResponse;
import com.moodify.dto.WeekRecommendationRequest;
import com.moodify.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecommendationService {

    private static final String PROMPT_VERSION_AI = "v1-ai";
    private static final String PROMPT_VERSION_FB = "v1-fallback";

    private final Map<String, RecommendationResponse> cache = new ConcurrentHashMap<>();
    private final OpenAIClient openAIClient;
    private final DailyMoodService dailyMoodService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public RecommendationService(OpenAIClient openAIClient, DailyMoodService dailyMoodService, UserService userService, ObjectMapper objectMapper) {
        this.openAIClient = openAIClient;
        this.dailyMoodService = dailyMoodService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public RecommendationResponse recommendDaily(DailyRecommendationRequest req) {
        int score = clampScore(req.getScore());
        String context = normalize(req.getContext());
        String baseCategory = toCategory(score);
        String extendedContext = context;
        String userKey = "anon";
        if (req.getUserId() != null) {
            try {
                User user = userService.getById(req.getUserId());
                extendedContext = enrichContextWithUserProfile(user, context);
                userKey = user.getId().toString();
            } catch (Exception ignored) {
            }
        }
        String key = "daily|user=" + userKey + "|score=" + score + "|ctx=" + extendedContext;

        RecommendationResponse cached = cache.get(key);
        if (cached != null) {
            RecommendationResponse copy = copyOf(cached);
            copy.setCached(true);
            return copy;
        }

        Optional<OpenAIClient.AiResult> ai = openAIClient.recommendActivities(score, baseCategory, extendedContext);
        if (ai.isPresent()) {
            OpenAIClient.AiResult r = ai.get();
            RecommendationResponse resp = new RecommendationResponse(
                    score,
                    fallback(r.getCategory(), baseCategory),
                    r.getActivities(),
                    r.getTips(),
                    PROMPT_VERSION_AI,
                    false
            );
            cache.put(key, resp);
            return copyOf(resp);
        }

        RecommendationResponse resp = new RecommendationResponse(
                score,
                baseCategory,
                fallbackActivities(baseCategory),
                fallbackTips(baseCategory),
                PROMPT_VERSION_FB,
                false
        );
        cache.put(key, resp);
        return copyOf(resp);
    }

    public RecommendationResponse recommendForWeek(WeekRecommendationRequest req) {
        User user = userService.getById(req.getUserId());
        int weekNumber = req.getWeekNumber();
        double avg = dailyMoodService.computeAverageMoodForWeek(user, weekNumber);

        int score = clampScore((int)Math.round(avg));
        String context = normalize(req.getContext());
        String extendedContext = enrichContextWithUserProfile(user, context);
        String baseCategory = toCategory(score);
        String key = "week|user=" + user.getId() + "|week=" + weekNumber + "|score=" + score + "|ctx=" + extendedContext;

        RecommendationResponse cached = cache.get(key);
        if (cached != null) {
            RecommendationResponse copy = copyOf(cached);
            copy.setCached(true);
            return copy;
        }

        Optional<OpenAIClient.AiResult> ai = openAIClient.recommendActivities(score, baseCategory, extendedContext);
        if (ai.isPresent()) {
            OpenAIClient.AiResult r = ai.get();
            RecommendationResponse resp = new RecommendationResponse(
                    score,
                    fallback(r.getCategory(), baseCategory),
                    r.getActivities(),
                    r.getTips(),
                    PROMPT_VERSION_AI,
                    false
            );
            cache.put(key, resp);
            return copyOf(resp);
        }

        RecommendationResponse resp = new RecommendationResponse(
                score,
                baseCategory,
                fallbackActivities(baseCategory),
                fallbackTips(baseCategory),
                PROMPT_VERSION_FB,
                false
        );
        cache.put(key, resp);
        return copyOf(resp);
    }

    private int clampScore(Integer s) {
        if (s == null) return 1;
        return Math.max(1, Math.min(5, s));
    }

    private String toCategory(int score) {
        // 1=angry, 2=sad, 3=neutral, 4=happy, 5=joy
        return switch (score) {
            case 1 -> "angry";
            case 2 -> "sad";
            case 3 -> "neutral";
            case 4 -> "happy";
            default -> "joy";
        };
    }

    private String normalize(String context) {
        if (context == null) return "";
        String s = context.trim();
        return s.length() > 200 ? s.substring(0, 200) : s;
    }

    private String fallback(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    private java.util.List<String> fallbackActivities(String category) {
        return switch (category) {
            case "angry" -> java.util.List.of(
                "Tarik napas 4-4-4-4 selama 3 menit",
                "Jeda sejenak jauhkan diri dari pemicu",
                "Tuliskan apa yang kamu rasakan (2-3 kalimat)",
                "Air putih dan cuci muka"
            );
            case "sad" -> java.util.List.of(
                "Hubungi teman/keluarga 5 menit",
                "Jalan pelan 10 menit sambil dengar musik lembut",
                "Tulis 1 hal yang kamu syukuri",
                "Peregangan ringan 5 menit"
            );
            case "neutral" -> java.util.List.of(
                "Rapikan meja 5 menit",
                "Kerjakan 1 tugas kecil (<=15 menit)",
                "Rencanakan 3 prioritas hari ini",
                "Minum air dan peregangan singkat"
            );
            case "happy" -> java.util.List.of(
                "Selesaikan 1 tugas menantang 25 menit",
                "Berbagi kebaikan kecil pada orang lain",
                "Latihan fisik singkat 10-15 menit",
                "Kembangkan hobi 20 menit"
            );
            default -> java.util.List.of(
                "Rayakan pencapaian kecil (tulis 1-2 baris)",
                "Bantu orang lain/berbagi cerita positif",
                "Rencanakan langkah progres untuk tujuanmu",
                "Nikmati momen mindful 3 menit"
            );
        };
    }

    private String fallbackTips(String category) {
        return switch (category) {
            case "angry" -> "Turunkan intensitas dulu, respon setelah tenang.";
            case "sad" -> "Beri ruang pada perasaan, bergerak pelan itu cukup.";
            case "neutral" -> "Mulai dari langkah kecil untuk bangun momentum.";
            case "happy" -> "Salurkan energi ke aktivitas bermakna dan terukur.";
            default -> "Rayakan momen baik sambil tetap menjaga ritme sehat.";
        };
    }

    private RecommendationResponse copyOf(RecommendationResponse r) {
        return new RecommendationResponse(
                r.getScore(), r.getCategory(), new ArrayList<>(r.getActivities()),
                r.getTips(), r.getPromptVersion(), r.isCached()
        );
    }

    private String enrichContextWithUserProfile(User user, String baseContext) {
        StringBuilder sb = new StringBuilder();
        if (baseContext != null && !baseContext.isBlank()) sb.append(baseContext.trim());

        // Age
        if (user.getBirthDate() != null) {
            try {
                int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
                if (age > 0 && age < 120) {
                    if (sb.length() > 0) sb.append(" | ");
                    sb.append("umur:").append(age);
                }
            } catch (Exception ignored) {}
        }

        // Gender
        if (user.getGender() != null && !user.getGender().isBlank()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("gender:").append(user.getGender());
        }

        // Hobbies
        String hj = user.getHobbiesJson();
        if (hj != null && !hj.isBlank()) {
            try {
                List<String> hobbies = objectMapper.readValue(hj, new TypeReference<List<String>>(){});
                if (hobbies != null && !hobbies.isEmpty()) {
                    if (sb.length() > 0) sb.append(" | ");
                    List<String> trimmed = hobbies.size() > 6 ? hobbies.subList(0, 6) : hobbies;
                    sb.append("hobi:").append(String.join(",", trimmed));
                }
            } catch (Exception ignored) {}
        }

        String out = sb.toString();
        return out.length() > 400 ? out.substring(0, 400) : out;
    }
}
