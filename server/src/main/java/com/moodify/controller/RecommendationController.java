package com.moodify.controller;

import com.moodify.dto.DailyRecommendationRequest;
import com.moodify.dto.RecommendationResponse;
import com.moodify.dto.WeekRecommendationRequest;
import com.moodify.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @PostMapping("/daily")
    public ResponseEntity<RecommendationResponse> daily(@Valid @RequestBody DailyRecommendationRequest request) {
        return ResponseEntity.ok(service.recommendDaily(request));
    }

    @PostMapping("/week")
    public ResponseEntity<RecommendationResponse> week(@Valid @RequestBody WeekRecommendationRequest request) {
        return ResponseEntity.ok(service.recommendForWeek(request));
    }
}
