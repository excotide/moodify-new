package com.moodify.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OpenAIClient {

    @Value("${openai.api.baseUrl:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.timeoutMillis:3500}")
    private int timeoutMillis;

    private final ObjectMapper mapper;

    public OpenAIClient(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<AiResult> recommendActivities(int score, String category, String context) {
        if (apiKey == null || apiKey.isBlank()) return Optional.empty();
        try {
            var client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMillis))
                    .build();

                        var systemPrompt = """
                                Kamu adalah asisten yang memberi rekomendasi aktivitas singkat berbasis skor mood.
                                Balas SELALU dalam format JSON dengan schema:
                                {
                                    "category": "angry|sad|neutral|happy|joy",
                                    "activities": ["string", ... minimal 3 maks 6],
                                    "tips": "string singkat"
                                }
                                Gunakan bahasa Indonesia, praktis, dan aman. Jangan sertakan penjelasan di luar JSON.
                                """;

            String userPrompt = "Skor: " + score + "\nKategori: " + category +
                    "\nPreferensi: " + (context == null ? "" : context);

            var body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.7,
                    "max_tokens", 300
            );

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofMillis(timeoutMillis))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) return Optional.empty();

            JsonNode root = mapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) return Optional.empty();

            JsonNode obj = mapper.readTree(content);
            String outCategory = obj.path("category").asText(category);
            List<String> activities = new ArrayList<>();
            if (obj.has("activities") && obj.get("activities").isArray()) {
                obj.get("activities").forEach(n -> { if (n.isTextual()) activities.add(n.asText()); });
            }
            String tips = obj.path("tips").asText("");

            if (activities.isEmpty() || tips.isBlank()) return Optional.empty();

            AiResult result = new AiResult(outCategory, activities, tips);
            return Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> commentOnReason(int score, String reason) {
        return commentOnReason(score, reason, null);
    }

    public Optional<String> commentOnReason(int score, String reason, String context) {
        if (apiKey == null || apiKey.isBlank()) return Optional.empty();
        if (reason == null || reason.isBlank()) return Optional.empty();
        try {
            var client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMillis))
                    .build();

            var systemPrompt = """
                Kamu adalah asisten ramah dan suportif. Balas singkat (maks 2 kalimat),
                empatik, dan relevan dengan alasan pengguna serta skor mood. berikan juga rekomendasi aktivitas tanpa poin(maks 2 kalimat) berdasarkan skor dari pengguna.
                Kembalikan SELALU JSON: {"comment":"..."} tanpa teks lain.
                Gunakan bahasa Indonesia.
                """;

            StringBuilder up = new StringBuilder();
            up.append("Skor: ").append(score).append('\n');
            up.append("Alasan: ").append(reason);
            if (context != null && !context.isBlank()) {
                up.append("\nProfil: ").append(context);
            }
            String userPrompt = up.toString();

            var body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.6,
                    "max_tokens", 120
            );

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofMillis(timeoutMillis))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) return Optional.empty();

            JsonNode root = mapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) return Optional.empty();

            JsonNode obj = mapper.readTree(content);
            String comment = obj.path("comment").asText("");
            if (comment.isBlank()) return Optional.empty();
            return Optional.of(comment);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static class AiResult {
        private final String category;
        private final List<String> activities;
        private final String tips;

        public AiResult(String category, List<String> activities, String tips) {
            this.category = category;
            this.activities = activities;
            this.tips = tips;
        }
        public String getCategory() { return category; }
        public List<String> getActivities() { return activities; }
        public String getTips() { return tips; }
    }

    public static class AiHealth {
        private final boolean configured;
        private final boolean reachable;
        private final String model;
        private final String error;

        public AiHealth(boolean configured, boolean reachable, String model, String error) {
            this.configured = configured;
            this.reachable = reachable;
            this.model = model;
            this.error = error;
        }
        public boolean isConfigured() { return configured; }
        public boolean isReachable() { return reachable; }
        public String getModel() { return model; }
        public String getError() { return error; }
    }

    /**
     * @param probe 
     */
    public AiHealth health(boolean probe) {
        boolean configured = apiKey != null && !apiKey.isBlank();
        if (!configured) {
            return new AiHealth(false, false, model, "API key missing");
        }
        if (!probe) {
            return new AiHealth(true, true, model, "OK (not probed)");
        }
        try {
            var client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(Math.min(timeoutMillis, 2000))).build();
            // OpenAI model detail endpoint
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/models/" + model))
                    .timeout(Duration.ofMillis(Math.min(timeoutMillis, 2000)))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 300) {
                return new AiHealth(true, true, model, "OK");
            } else {
                return new AiHealth(true, false, model, "HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            return new AiHealth(true, false, model, e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "error" : e.getMessage()));
        }
    }

    public Optional<String> weeklySummaryComment(String breakdownText) {
        return weeklySummaryComment(breakdownText, null);
    }

    public Optional<String> weeklySummaryComment(String breakdownText, String context) {
        if (apiKey == null || apiKey.isBlank()) return Optional.empty();
        try {
            var client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMillis))
                    .build();

            var systemPrompt = """
                Kamu adalah asisten ramah. Analisis mood dari pengguna (maks 4 kalimat) dan berikan saran untuk minggu kedepannya. Kembalikan SELALU JSON: {"comment":"..."}.
                FORMAT: komentar harus diawali dengan teks ringkasan persentase yang diberikan,
                diikuti tanda em dash (—), lalu 1-2 kalimat yang suportif.
                Contoh: "62.5% Sad, 25% Angry, 12.5% Happy — kalimat dukungan positif..."
                Gunakan bahasa Indonesia.
                """;

            StringBuilder up = new StringBuilder();
            up.append("Ringkasan: ").append(breakdownText);
            if (context != null && !context.isBlank()) {
                up.append("\nProfil: ").append(context);
            }
            up.append("\nBuat komentar sesuai format.");
            String userPrompt = up.toString();

            var body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.6,
                    "max_tokens", 160
            );

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofMillis(timeoutMillis))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) return Optional.empty();

            JsonNode root = mapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) return Optional.empty();

            JsonNode obj = mapper.readTree(content);
            String comment = obj.path("comment").asText("");
            if (comment.isBlank()) return Optional.empty();
            return Optional.of(comment);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
