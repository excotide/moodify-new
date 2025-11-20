package com.moodify.service;

import com.moodify.entity.DailyMoodEntry;
import com.moodify.entity.User;
import com.moodify.repository.DailyMoodEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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
     * Submit mood for a past date (before today). Creates the entry if missing.
     * Rules:
     *  - date must be < today (not future, not today)
     *  - date must be >= anchor (firstLogin or createdAt) otherwise rejected
     *  - if an entry exists and mood already filled -> reject
     */
    @Transactional
    public DailyMoodEntry submitPastMood(User user, LocalDate targetDate, Integer moodValue) {
        LocalDate today = LocalDate.now();
        if (!targetDate.isBefore(today)) {
            throw new IllegalArgumentException("Date must be in the past (before today)");
        }
        LocalDate anchor = null;
        if (user.getFirstLogin() != null) {
            anchor = user.getFirstLogin().toLocalDate();
        } else if (user.getCreatedAt() != null) {
            anchor = user.getCreatedAt().toLocalDate();
        }
        if (anchor != null && targetDate.isBefore(anchor)) {
            throw new IllegalArgumentException("Date is before user's tracking start");
        }
        Optional<DailyMoodEntry> opt = repo.findByUserAndDate(user, targetDate);
        DailyMoodEntry entry;
        if (opt.isPresent()) {
            entry = opt.get();
            if (entry.getMood() != null) {
                throw new IllegalStateException("Mood already submitted for this date");
            }
            entry.setMood(moodValue);
            entry.setCreatedAt(OffsetDateTime.now());
        } else {
            int weekNum = computeRelativeWeekNumber(user, targetDate);
            entry = new DailyMoodEntry(user, targetDate, weekNum);
            entry.setMood(moodValue);
            entry.setCreatedAt(OffsetDateTime.now());
        }
        return repo.save(entry);
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
        // Fill missing dates for window [today..end] using RELATIVE week logic starting from user's first login (or createdAt).
        IntStream.rangeClosed(0, 6).forEach(i -> {
            LocalDate d = today.plusDays(i);
            if (!existingDates.contains(d)) {
                int weekNum = computeRelativeWeekNumber(user, d);
                DailyMoodEntry me = new DailyMoodEntry(user, d, weekNum);
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

    @Transactional(readOnly = true)
    public java.util.List<DailyMoodEntry> getHistoryFromFirstToLastLogin(User user) {
        LocalDate start;
        if (user.getFirstLogin() != null) {
            start = user.getFirstLogin().toLocalDate();
        } else if (user.getCreatedAt() != null) {
            start = user.getCreatedAt().toLocalDate();
        } else {
            start = LocalDate.now();
        }
        LocalDate end;
        if (user.getLastLogin() != null) {
            end = user.getLastLogin().toLocalDate();
        } else {
            end = LocalDate.now();
        }
        if (end.isBefore(start)) {
            end = start;
        }
        return repo.findByUserAndDateBetween(user, start, end);
    }

    /**
     * Relative week number logic:
     *   week 1 starts on the user's first login date (firstLogin).
     *   If firstLogin is null we fall back to createdAt.
     *   If both are null (should not happen after persistence) we fall back to 'date' itself (result => week 1).
     * Formula: weeks = floor( daysBetween(startDate, date) / 7 ) + 1
     */
    private int computeRelativeWeekNumber(User user, LocalDate date) {
        LocalDate startDate = null;
        if (user.getFirstLogin() != null) {
            startDate = user.getFirstLogin().toLocalDate();
        } else if (user.getCreatedAt() != null) {
            startDate = user.getCreatedAt().toLocalDate();
        }
        if (startDate == null) {
            startDate = date; // safety fallback
        }
        long days = ChronoUnit.DAYS.between(startDate, date);
        if (days < 0) {
            return 1; // if somehow date precedes startDate
        }
        return (int) (days / 7) + 1;
    }
}
