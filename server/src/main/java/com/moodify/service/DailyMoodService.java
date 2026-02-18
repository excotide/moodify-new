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
            if (entry.getWeekNumber() == null) {
                entry.setWeekNumber(computeRelativeWeekNumber(user, today));
            }
            if (entry.getDayName() == null) {
                entry.setDayName(today.getDayOfWeek().toString());
            }
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

    public java.util.List<DailyMoodEntry> getUpcomingWeek(User user) {
        return getCurrentWeek(user);
    }

    @Transactional
    public java.util.List<DailyMoodEntry> getWeek(User user, int weekNumber) {
        int sanitizedWeekNumber = (weekNumber < 1) ? 1 : weekNumber;
        LocalDate anchor = getAnchorDate(user);
        LocalDate start = anchor.plusDays((long) (sanitizedWeekNumber - 1) * 7);
        LocalDate end = start.plusDays(6);
        LocalDate today = LocalDate.now();

        var existing = repo.findByUserAndDateBetween(user, start, end);
        var existingDates = existing.stream().map(DailyMoodEntry::getDate).toList();

        IntStream.rangeClosed(0, 6).forEach(i -> {
            LocalDate d = start.plusDays(i);
            if (d.isAfter(today)) return; 
            if (!existingDates.contains(d)) {
                DailyMoodEntry me = new DailyMoodEntry(user, d, sanitizedWeekNumber);
                repo.save(me);
            }
        });

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

    private int computeRelativeWeekNumber(User user, LocalDate date) {
        LocalDate startDate = null;
        if (user.getFirstLogin() != null) {
            startDate = user.getFirstLogin().toLocalDate();
        } else if (user.getCreatedAt() != null) {
            startDate = user.getCreatedAt().toLocalDate();
        }
        if (startDate == null) {
            startDate = date; 
        }
        long days = ChronoUnit.DAYS.between(startDate, date);
        if (days < 0) {
            return 1; 
        }
        return (int) (days / 7) + 1;
    }

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

    @Transactional
    public java.util.List<DailyMoodEntry> getCurrentWeek(User user) {
        LocalDate today = LocalDate.now();
        int currentWeek = computeRelativeWeekNumber(user, today);
        LocalDate anchor = getAnchorDate(user);
        LocalDate start = anchor.plusDays((long) (currentWeek - 1) * 7);
        LocalDate end = start.plusDays(6);

        var existing = repo.findByUserAndDateBetween(user, start, end);
        var existingDates = existing.stream().map(DailyMoodEntry::getDate).toList();

        IntStream.rangeClosed(0, 6).forEach(i -> {
            LocalDate d = start.plusDays(i);
            if (!existingDates.contains(d)) {
                int weekNum = currentWeek; 
                DailyMoodEntry me = new DailyMoodEntry(user, d, weekNum);
                repo.save(me);
            }
        });

        var weekEntries = repo.findByUserAndDateBetween(user, start, end);
        weekEntries.forEach(e -> {
            if (e.getWeekNumber() == null) e.setWeekNumber(computeRelativeWeekNumber(user, e.getDate()));
            if (e.getDayName() == null) e.setDayName(e.getDate().getDayOfWeek().toString());
        });
        return weekEntries.stream()
                .sorted(java.util.Comparator.comparing(DailyMoodEntry::getDate))
                .toList();
    }
}
