package com.moodify.service;

import com.moodify.ai.OpenAIClient;
import com.moodify.dto.DailyRecommendationRequest;
import com.moodify.dto.RecommendationResponse;
import com.moodify.dto.WeekRecommendationRequest;
import com.moodify.entity.User;
import org.springframework.stereotype.Service;

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

    public RecommendationService(OpenAIClient openAIClient, DailyMoodService dailyMoodService, UserService userService) {
        this.openAIClient = openAIClient;
        this.dailyMoodService = dailyMoodService;
        this.userService = userService;
    }

    public RecommendationResponse recommendDaily(DailyRecommendationRequest req) {
        int score = clampScore(req.getScore());
        String context = normalize(req.getContext());
        String baseCategory = toCategory(score);
        String key = "daily|score=" + score + "|ctx=" + context;

        RecommendationResponse cached = cache.get(key);
        if (cached != null) {
            RecommendationResponse copy = copyOf(cached);
            copy.setCached(true);
            return copy;
        }

        Optional<OpenAIClient.AiResult> ai = openAIClient.recommendActivities(score, baseCategory, context);
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
        String baseCategory = toCategory(score);
        String key = "week|user=" + user.getId() + "|week=" + weekNumber + "|score=" + score + "|ctx=" + context;

        RecommendationResponse cached = cache.get(key);
        if (cached != null) {
            RecommendationResponse copy = copyOf(cached);
            copy.setCached(true);
            return copy;
        }

        Optional<OpenAIClient.AiResult> ai = openAIClient.recommendActivities(score, baseCategory, context);
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
        if (s == null) return 0;
        return Math.max(0, Math.min(5, s));
    }

    private String toCategory(int score) {
        if (score <= 1) return "jelek";
        if (score <= 3) return "netral";
        return "bagus";
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
        switch (category) {
            case "jelek":
                return java.util.List.of(
                        "Tarik napas 4-4-4-4 selama 3 menit",
                        "Jalan kaki ringan 10 menit",
                        "Musik relaksasi 10 menit",
                        "Catat 1 langkah kecil yang bisa dilakukan"
                );
            case "netral":
                return java.util.List.of(
                        "Rapikan meja 5 menit",
                        "Kerjakan 1 tugas kecil (<=15 menit)",
                        "Peregangan tubuh 5 menit",
                        "Tentukan 3 prioritas hari ini"
                );
            default:
                return java.util.List.of(
                        "Sesi fokus 25 menit (pomodoro)",
                        "Latihan fisik singkat",
                        "Beri apresiasi rekan/teman",
                        "Hobi kreatif 20 menit"
                );
        }
    }

    private String fallbackTips(String category) {
        switch (category) {
            case "jelek":
                return "Ambil langkah kecil, batasi distraksi, istirahat singkat.";
            case "netral":
                return "Bangun momentum dengan kebiasaan kecil yang konsisten.";
            default:
                return "Salurkan energi positif ke tugas berdampak, jaga ritme.";
        }
    }

    private RecommendationResponse copyOf(RecommendationResponse r) {
        return new RecommendationResponse(
                r.getScore(), r.getCategory(), new ArrayList<>(r.getActivities()),
                r.getTips(), r.getPromptVersion(), r.isCached()
        );
    }
}
