package com.leetcode.analyzer.analytics;

import com.leetcode.analyzer.database.DatabaseManager;
import com.leetcode.analyzer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Performs analytics over a user's LeetCode history.
 *
 * <p>All analysis is driven by database queries and processed via
 * Java Streams API for efficient, readable aggregations.</p>
 */
public class AnalyticsEngine {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsEngine.class);
    private final DatabaseManager dbManager;

    public AnalyticsEngine(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Returns a map of categories sorted by success rate ascending (worst first).
     *
     * <p>Time complexity: O(n log n) where n = number of categories with activity.
     *
     * @param userId the user's id
     * @return LinkedHashMap of category → success rate (0.0 – 1.0), sorted weakest first
     */
    public Map<Category, Double> getWeakAreas(int userId) {
        try {
            Map<String, int[]> stats = dbManager.getCategoryStats(userId);
            return stats.entrySet().stream()
                .filter(e -> e.getValue()[1] > 0) // attempted > 0
                .collect(Collectors.toMap(
                    e -> Category.valueOf(e.getKey()),
                    e -> (double) e.getValue()[0] / e.getValue()[1],
                    (a, b) -> a,
                    LinkedHashMap::new
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue())  // ascending = weakest first
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (a, b) -> a,
                    LinkedHashMap::new
                ));
        } catch (SQLException e) {
            logger.error("Failed to retrieve weak areas", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Calculates the success rate (0.0–1.0) for a specific category.
     *
     * @param userId   the user's id
     * @param category the category to check
     * @return success rate, or 0.0 if no data
     */
    public double calculateSuccessRate(int userId, Category category) {
        try {
            Map<String, int[]> stats = dbManager.getCategoryStats(userId);
            int[] data = stats.get(category.name());
            if (data == null || data[1] == 0) return 0.0;
            return (double) data[0] / data[1];
        } catch (SQLException e) {
            logger.error("Failed to calculate success rate for {}", category, e);
            return 0.0;
        }
    }

    /**
     * Returns problem-solve counts grouped by date within a date range.
     *
     * @param userId    the user's id
     * @param startDate inclusive start
     * @param endDate   inclusive end
     * @return sorted map of date → number of problems solved that day
     */
    public Map<LocalDate, Long> getProgressOverTime(int userId, LocalDate startDate, LocalDate endDate) {
        try {
            List<Solution> solutions = dbManager.getUserSolutions(userId);
            return solutions.stream()
                .filter(s -> s.isAccepted() && s.getSolvedAt() != null)
                .filter(s -> {
                    LocalDate d = s.getSolvedAt().toLocalDate();
                    return !d.isBefore(startDate) && !d.isAfter(endDate);
                })
                .collect(Collectors.groupingBy(
                    s -> s.getSolvedAt().toLocalDate(),
                    TreeMap::new,
                    Collectors.counting()
                ));
        } catch (SQLException e) {
            logger.error("Failed to calculate progress over time", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Returns the top N categories by solve count.
     *
     * @param userId the user's id
     * @param limit  maximum number of categories to return
     * @return ordered list of top categories
     */
    public List<Category> getTopCategories(int userId, int limit) {
        try {
            Map<String, int[]> stats = dbManager.getCategoryStats(userId);
            return stats.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]))
                .limit(limit)
                .map(e -> Category.valueOf(e.getKey()))
                .collect(Collectors.toList());
        } catch (SQLException e) {
            logger.error("Failed to get top categories", e);
            return Collections.emptyList();
        }
    }

    /**
     * Returns counts of solved problems per difficulty level.
     *
     * @param userId the user's id
     * @return map of difficulty → count
     */
    public Map<Difficulty, Integer> getDifficultySummary(int userId) {
        try {
            Map<String, Integer> raw = dbManager.getDifficultyStats(userId);
            Map<Difficulty, Integer> result = new EnumMap<>(Difficulty.class);
            for (Difficulty d : Difficulty.values()) result.put(d, 0);
            raw.forEach((k, v) -> {
                try { result.put(Difficulty.valueOf(k), v); }
                catch (IllegalArgumentException ignored) {}
            });
            return result;
        } catch (SQLException e) {
            logger.error("Failed to get difficulty summary", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Generates a full Statistics snapshot for the user.
     *
     * @param userId the user's id
     * @return populated Statistics object
     */
    public Statistics generateStatistics(int userId) {
        Statistics stats = new Statistics();
        try {
            List<Solution> solutions = dbManager.getUserSolutions(userId);

            long accepted = solutions.stream().filter(Solution::isAccepted).count();
            stats.setTotalSolved((int) accepted);
            stats.setTotalAttempted(solutions.size());

            double successRate = solutions.isEmpty() ? 0.0 : (double) accepted / solutions.size();
            stats.setOverallSuccessRate(successRate);

            double avgAttempts = solutions.stream()
                .mapToInt(Solution::getAttempts)
                .average()
                .orElse(0.0);
            stats.setAverageAttempts(avgAttempts);

            // Category breakdowns
            Map<String, int[]> catStats = dbManager.getCategoryStats(userId);
            Map<Category, Integer> solvedByCategory = new HashMap<>();
            Map<Category, Double> successByCategory = new HashMap<>();
            catStats.forEach((cat, data) -> {
                try {
                    Category c = Category.valueOf(cat);
                    solvedByCategory.put(c, data[0]);
                    successByCategory.put(c, data[1] > 0 ? (double) data[0] / data[1] : 0.0);
                } catch (IllegalArgumentException ignored) {}
            });
            stats.setSolvedByCategory(solvedByCategory);
            stats.setSuccessRateByCategory(successByCategory);

            // Difficulty breakdown
            stats.setSolvedByDifficulty(getDifficultySummary(userId));

            // Strongest and weakest
            if (!successByCategory.isEmpty()) {
                stats.setStrongestCategory(
                    successByCategory.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(e -> e.getKey().getDisplayName())
                        .orElse("N/A")
                );
                stats.setWeakestCategory(
                    successByCategory.entrySet().stream()
                        .min(Map.Entry.comparingByValue())
                        .map(e -> e.getKey().getDisplayName())
                        .orElse("N/A")
                );
            }

        } catch (SQLException e) {
            logger.error("Failed to generate statistics", e);
        }
        return stats;
    }

    /**
     * Returns a streak count: how many consecutive days the user solved at least one problem.
     *
     * @param userId the user's id
     * @return current streak in days
     */
    public int getCurrentStreak(int userId) {
        try {
            List<Solution> solutions = dbManager.getUserSolutions(userId);
            Set<LocalDate> activeDays = solutions.stream()
                .filter(s -> s.isAccepted() && s.getSolvedAt() != null)
                .map(s -> s.getSolvedAt().toLocalDate())
                .collect(Collectors.toSet());

            if (activeDays.isEmpty()) return 0;

            int streak = 0;
            LocalDate current = LocalDate.now();
            while (activeDays.contains(current)) {
                streak++;
                current = current.minusDays(1);
            }
            return streak;
        } catch (SQLException e) {
            logger.error("Failed to calculate streak", e);
            return 0;
        }
    }
}
