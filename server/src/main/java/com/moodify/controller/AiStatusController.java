package com.moodify.controller;

import com.moodify.ai.OpenAIClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AiStatusController {

    private final OpenAIClient openAIClient;

    public AiStatusController(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @GetMapping("/api/ai/status")
    public Map<String, Object> status(@RequestParam(name = "probe", defaultValue = "false") boolean probe) {
        var health = openAIClient.health(probe);
        return Map.of(
                "configured", health.isConfigured(),
                "reachable", health.isReachable(),
                "model", health.getModel(),
                "error", health.getError()
        );
    }
}
