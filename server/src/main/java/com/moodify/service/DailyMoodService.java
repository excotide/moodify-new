package com.moodify.service;

import com.moodify.entity.DailyMoodEntry;
import com.moodify.entity.User;
import com.moodify.repository.DailyMoodEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class DailyMoodService {

    @Autowired
    private DailyMoodEntryRepository repo;

    @Transactional
    public void initializeWeekIfFirstLogin(User user) {
        // Ensure there are placeholders for the upcoming 7 days (sliding window).
        ensureUpcoming7Days(user);
    }

    @Transactional
    public DailyMoodEntry submitTodayMood(User user, Integer moodValue) {
        LocalDate today = LocalDate.now();
        Optional<DailyMoodEntry> opt = repo.findByUserAndDate(user, today);
        DailyMoodEntry entry;
        if (opt.isPresent()) {
            entry = opt.get();
            if (entry.getMood() != null) {
                throw new IllegalStateException("Mood already submitted for today");
            }
            entry.setMood(moodValue);
            entry.setCreatedAt(OffsetDateTime.now());
            // After submitting, ensure placeholders exist for the upcoming week (sliding window)
            ensureUpcoming7Days(user);
            return repo.save(entry);
        } else {
            entry = new DailyMoodEntry(user, today, moodValue, OffsetDateTime.now());
            DailyMoodEntry saved = repo.save(entry);
            ensureUpcoming7Days(user);
            return saved;
        }
    }

    /**
     * Ensure there are DailyMoodEntry rows for each date in [today .. today+6].
     * Creates missing placeholders (mood == null) as needed to keep a 7-day sliding window.
     */
    @Transactional
    protected void ensureUpcoming7Days(User user) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(6);
        // fetch existing entries in the window
        var existing = repo.findByUserAndDateBetween(user, today, end);
        var existingDates = existing.stream().map(DailyMoodEntry::getDate).toList();

        // Determine last known week info
        var lastOpt = repo.findTopByUserOrderByDateDesc(user);
        final LocalDate lastDate;
        final int baseNextWeek;
        final LocalDate startForNext;
        if (lastOpt.isPresent()) {
            DailyMoodEntry last = lastOpt.get();
            lastDate = last.getDate();
            baseNextWeek = (last.getWeekNumber() == null ? 1 : last.getWeekNumber() + 1);
            startForNext = lastDate.plusDays(1);
        } else {
            lastDate = null;
            baseNextWeek = 1;
            startForNext = today;
        }

        // If there are missing dates in the immediate window [today..end], create them.
        IntStream.rangeClosed(0, 6).forEach(i -> {
            LocalDate d = today.plusDays(i);
            if (!existingDates.contains(d)) {
                // If this date is after the last known date, assign it to the nextWeekNumber,
                // otherwise try to infer weekNumber from existing entries (fallback to 1)
                int weekNumToUse = 1;
                if (lastDate != null && d.isAfter(lastDate)) {
                    long offset = java.time.temporal.ChronoUnit.DAYS.between(startForNext, d);
                    weekNumToUse = baseNextWeek + (int) (offset / 7);
                } else {
                    // try find existing entry on same week window
                    weekNumToUse = existing.stream().filter(e -> e.getDate().equals(d)).findFirst().map(DailyMoodEntry::getWeekNumber).orElse(1);
                }
                DailyMoodEntry me = new DailyMoodEntry(user, d, weekNumToUse);
                repo.save(me);
            }
        });
    }

    /**
     * Return the list of DailyMoodEntry objects for the upcoming 7 days (today..today+6).
     * Ensures placeholders exist first.
     */
    @Transactional(readOnly = true)
    public java.util.List<DailyMoodEntry> getUpcomingWeek(User user) {
        // create missing placeholders if needed
        ensureUpcoming7Days(user);
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(6);
        return repo.findByUserAndDateBetween(user, today, end);
    }
}
