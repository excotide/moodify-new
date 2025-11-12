package com.moodify.repository;

import com.moodify.entity.DailyMoodEntry;
import com.moodify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyMoodEntryRepository extends JpaRepository<DailyMoodEntry, UUID> {
    Optional<DailyMoodEntry> findByUserAndDate(User user, LocalDate date);
    List<DailyMoodEntry> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);
    long countByUser(User user);
    Optional<DailyMoodEntry> findTopByUserOrderByDateDesc(User user);
}
