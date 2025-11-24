package com.moodify.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moodify.entity.DailyMoodEntry;
import com.moodify.entity.User;
import com.moodify.repository.DailyMoodEntryRepository;

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
        return submitTodayMood(user, moodValue, null);
    }

    @Transactional
    public DailyMoodEntry submitTodayMood(User user, Integer moodValue, String reason) {
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
            if (reason != null && !reason.isBlank()) entry.setReason(reason);
            // Populate missing meta if absent
            if (entry.getWeekNumber() == null) {
                entry.setWeekNumber(computeRelativeWeekNumber(user, today));
            }
            if (entry.getDayName() == null) {
                entry.setDayName(today.getDayOfWeek().toString());
            }
            // After submitting, ensure placeholders exist for the upcoming week (sliding window)
            ensureUpcoming7Days(user);
            return repo.save(entry);
        } else {
            entry = new DailyMoodEntry(user, today, moodValue, OffsetDateTime.now());
            entry.setWeekNumber(computeRelativeWeekNumber(user, today));
            entry.setDayName(today.getDayOfWeek().toString());
            if (reason != null && !reason.isBlank()) entry.setReason(reason);
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
        return submitPastMood(user, targetDate, moodValue, null);
    }

    @Transactional
    public DailyMoodEntry submitPastMood(User user, LocalDate targetDate, Integer moodValue, String reason) {
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
            if (reason != null && !reason.isBlank()) entry.setReason(reason);
            if (entry.getWeekNumber() == null) {
                entry.setWeekNumber(computeRelativeWeekNumber(user, targetDate));
            }
            if (entry.getDayName() == null) {
                entry.setDayName(targetDate.getDayOfWeek().toString());
            }
        } else {
            int weekNum = computeRelativeWeekNumber(user, targetDate);
            entry = new DailyMoodEntry(user, targetDate, weekNum);
            entry.setMood(moodValue);
            entry.setCreatedAt(OffsetDateTime.now());
            if (reason != null && !reason.isBlank()) entry.setReason(reason);
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
        return getCurrentWeek(user);
    }

    /**
     * Get entries for a specific relative week number (week 1 starts at anchor).
     * Creates placeholders for missing days within that week range up to today.
     * If the requested week is in the future (all dates > today) it just returns existing entries (likely empty).
     */
    @Transactional
    public java.util.List<DailyMoodEntry> getWeek(User user, int weekNumber) {
        int sanitizedWeekNumber = (weekNumber < 1) ? 1 : weekNumber;
        LocalDate anchor = getAnchorDate(user);
        LocalDate start = anchor.plusDays((long) (sanitizedWeekNumber - 1) * 7);
        LocalDate end = start.plusDays(6);
        LocalDate today = LocalDate.now();

        var existing = repo.findByUserAndDateBetween(user, start, end);
        var existingDates = existing.stream().map(DailyMoodEntry::getDate).toList();

        // Only create placeholders for dates that are not in the future.
        IntStream.rangeClosed(0, 6).forEach(i -> {
            LocalDate d = start.plusDays(i);
            if (d.isAfter(today)) return; // skip future dates
            if (!existingDates.contains(d)) {
                DailyMoodEntry me = new DailyMoodEntry(user, d, sanitizedWeekNumber);
                repo.save(me);
            }
        });

        // Re-fetch after possible placeholder creation
        var weekEntries = repo.findByUserAndDateBetween(user, start, end);
        weekEntries.forEach(e -> {
            if (e.getWeekNumber() == null) e.setWeekNumber(computeRelativeWeekNumber(user, e.getDate()));
            if (e.getDayName() == null) e.setDayName(e.getDate().getDayOfWeek().toString());
        });
        return weekEntries.stream()
                .sorted(java.util.Comparator.comparing(DailyMoodEntry::getDate))
                .toList();
    }

    @Transactional(readOnly = true)
    public double computeAverageMoodForWeek(User user, int weekNumber) {
        var entries = getHistoryFromFirstToLastLogin(user);
        var stats = entries.stream()
                .filter(e -> e.getWeekNumber() != null && e.getWeekNumber() == weekNumber)
                .filter(e -> e.getMood() != null)
                .mapToInt(DailyMoodEntry::getMood)
                .summaryStatistics();
        if (stats.getCount() == 0) {
            throw new IllegalStateException("No mood entries for the given week");
        }
        double avg = stats.getAverage();
        if (avg < 0) avg = 0;
        if (avg > 5) avg = 5;
        return avg;
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

    // Anchor date (firstLogin -> createdAt -> today)
    private LocalDate getAnchorDate(User user) {
        if (user.getFirstLogin() != null) return user.getFirstLogin().toLocalDate();
        if (user.getCreatedAt() != null) return user.getCreatedAt().toLocalDate();
        return LocalDate.now();
    }
    
    @Transactional
    public DailyMoodEntry addAiComment(DailyMoodEntry entry, String comment) {
        if (entry == null || comment == null || comment.isBlank()) return entry;
        entry.setAiComment(comment);
        return repo.save(entry);
    }

    /**
     * Return entries hanya untuk minggu relatif saat ini (bukan sliding 7 hari ke depan).
     * Jika ada tanggal dalam minggu ini yang belum punya entri, buat placeholder.
     * Pastikan setiap entri memiliki weekNumber dan dayName terisi.
     */
    @Transactional
    public java.util.List<DailyMoodEntry> getCurrentWeek(User user) {
        LocalDate today = LocalDate.now();
        int currentWeek = computeRelativeWeekNumber(user, today);
        LocalDate anchor = getAnchorDate(user);
        LocalDate start = anchor.plusDays((long) (currentWeek - 1) * 7);
        LocalDate end = start.plusDays(6);

        var existing = repo.findByUserAndDateBetween(user, start, end);
        var existingDates = existing.stream().map(DailyMoodEntry::getDate).toList();

        // Buat placeholder untuk tanggal yg belum ada
        IntStream.rangeClosed(0, 6).forEach(i -> {
            LocalDate d = start.plusDays(i);
            if (!existingDates.contains(d)) {
                int weekNum = currentWeek; // semua dalam minggu ini
                DailyMoodEntry me = new DailyMoodEntry(user, d, weekNum);
                repo.save(me);
            }
        });

        // Re-fetch setelah kemungkinan penambahan
        var weekEntries = repo.findByUserAndDateBetween(user, start, end);
        // Backfill metadata yg null
        weekEntries.forEach(e -> {
            if (e.getWeekNumber() == null) e.setWeekNumber(computeRelativeWeekNumber(user, e.getDate()));
            if (e.getDayName() == null) e.setDayName(e.getDate().getDayOfWeek().toString());
        });
        return weekEntries.stream()
                .sorted(java.util.Comparator.comparing(DailyMoodEntry::getDate))
                .toList();
    }
}
