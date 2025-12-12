package com.moodify.service;

import com.moodify.dto.MoodEntryRequest;
import com.moodify.dto.MoodEntryResponse;
import com.moodify.entity.MoodEntry;
import com.moodify.entity.User;
import com.moodify.repository.MoodEntryRepository;
import com.moodify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class MoodEntryService {

    @Autowired
    private MoodEntryRepository moodEntryRepository;
    @Autowired
    private UserRepository userRepository;

    public MoodEntryResponse createMoodEntry(MoodEntryRequest request) {
        User user = userRepository.findById(Objects.requireNonNull(request.getUserId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        MoodEntry moodEntry = new MoodEntry(user, request.getMood(), request.getScore());
        MoodEntry savedEntry = moodEntryRepository.save(moodEntry);
        return new MoodEntryResponse(
            Objects.requireNonNull(savedEntry.getId()),
            Objects.requireNonNull(user.getId()),
            savedEntry.getMood(),
            savedEntry.getScore(),
            savedEntry.getTimestamp()
        );
    }

    public List<MoodEntryResponse> getAllMoodEntries() {
        List<MoodEntry> entries = moodEntryRepository.findAll();
        return entries.stream()
                .map(entry -> new MoodEntryResponse(
                    Objects.requireNonNull(entry.getId()),
                    Objects.requireNonNull(entry.getUser().getId()),
                    entry.getMood(), entry.getScore(), entry.getTimestamp()
                ))
                .toList();
    }

    public Optional<MoodEntryResponse> getMoodEntryById(UUID id) {
        return moodEntryRepository.findById(Objects.requireNonNull(id))
            .map(entry -> new MoodEntryResponse(
                Objects.requireNonNull(entry.getId()),
                Objects.requireNonNull(entry.getUser().getId()),
                entry.getMood(), entry.getScore(), entry.getTimestamp()
            ));
    }

    public MoodEntryResponse getMoodEntry(UUID id) {
        return getMoodEntryById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mood entry not found"));
    }

    public MoodEntryResponse updateMoodEntry(UUID id, MoodEntryRequest request) {
        MoodEntry moodEntry = moodEntryRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mood entry not found"));
        moodEntry.setMood(request.getMood());
        moodEntry.setScore(request.getScore());
        MoodEntry updatedEntry = moodEntryRepository.save(moodEntry);
        return new MoodEntryResponse(
            Objects.requireNonNull(updatedEntry.getId()),
            Objects.requireNonNull(updatedEntry.getUser().getId()),
            updatedEntry.getMood(), updatedEntry.getScore(), updatedEntry.getTimestamp()
        );
    }

    public void deleteMoodEntry(UUID id) {
        moodEntryRepository.deleteById(Objects.requireNonNull(id));
    }
}