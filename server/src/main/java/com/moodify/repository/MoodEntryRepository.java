package com.moodify.repository;

import com.moodify.entity.MoodEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface  MoodEntryRepository extends JpaRepository<MoodEntry, UUID> {
}