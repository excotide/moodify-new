package com.moodify.controller;

import com.moodify.dto.MoodEntryRequest;
import com.moodify.dto.MoodEntryResponse;
import com.moodify.service.MoodEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mood-entries")
public class MoodEntryController {

    @Autowired
    private MoodEntryService moodEntryService;

    @PostMapping
    public ResponseEntity<MoodEntryResponse> createMoodEntry(@RequestBody MoodEntryRequest moodEntryRequest) {
        MoodEntryResponse response = moodEntryService.createMoodEntry(moodEntryRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MoodEntryResponse> getMoodEntry(@PathVariable UUID id) {
        MoodEntryResponse response = moodEntryService.getMoodEntry(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<MoodEntryResponse>> getAllMoodEntries() {
        List<MoodEntryResponse> responses = moodEntryService.getAllMoodEntries();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MoodEntryResponse> updateMoodEntry(@PathVariable UUID id, @RequestBody MoodEntryRequest moodEntryRequest) {
        MoodEntryResponse response = moodEntryService.updateMoodEntry(id, moodEntryRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMoodEntry(@PathVariable UUID id) {
        moodEntryService.deleteMoodEntry(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}