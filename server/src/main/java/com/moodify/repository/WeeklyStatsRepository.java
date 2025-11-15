package com.moodify.repository;

import com.moodify.entity.User;
import com.moodify.entity.WeeklyStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WeeklyStatsRepository extends JpaRepository<WeeklyStats, UUID> {
    Optional<WeeklyStats> findByUserAndWeekNumber(User user, Integer weekNumber);
}
