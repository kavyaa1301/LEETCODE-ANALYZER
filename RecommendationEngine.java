package com.leetcode.analyzer.recommendation;

import com.leetcode.analyzer.analytics.AnalyticsEngine;
import com.leetcode.analyzer.database.DatabaseManager;
import com.leetcode.analyzer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates personalized LeetCode problem recommendations based on a user's history.
 *
 * <p>Algorithm overview:
 * <ol>
 *   <li>Weak area detection – identifies categories with sub-50% success rate.</li>
 *   <li>Difficulty prediction – recommends an appropriate difficulty based on overall success rate.</li>
 *   <li>Learning path – builds a week-by-week schedule that gradually increases difficulty
 *       while ensuring category diversity.</li>
 * </ol>
 */
public class RecommendationEngine {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationEngine.class);
    private static final int DEFAULT_RECOMMENDATIONS = 8;
    private static final double WEAK_AREA_THRESHOLD = 0.5;

    private final DatabaseManager dbManager;
    private final AnalyticsEngine analyticsEngine;

    /**
     * Constructs the engine with its required dependencies.
     *
     * @param dbManager       the active database manager
     * @param analyticsEngine the analytics engine for user stats
     */
    public RecommendationEngine(DatabaseManager dbManager, AnalyticsEngine analyticsEngine) {
        this.dbManager = dbManager;
        this.analyticsEngine = analyticsEngine;
    }

    /**
     * Returns recommended problems targeting the user's weakest categories.
     * Problems are sorted by acceptance rate descending (easier first) to build confidence.
     *
     * @param user the user profile
     * @return list of up to 10 recommended problems
     */
    public List<Problem> recommendByWeakArea(UserProfile user) {
        Map<Category, Double> weakAreas = analyticsEngine.getWeakAreas(user.getId());
        List<Problem> recommendations = new ArrayList<>();

        // Focus on categories below threshold
        List<Category> weakCategories = weakAreas.entrySet().stream()
                .filter(e -> e.getValue() < WEAK_AREA_THRESHOLD)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (weakCategories.isEmpty()) {
            // All categories are fine; pick the two lowest-rated ones
            weakCategories = weakAreas.keySet().stream()
                    .limit(2)
                    .collect(Collectors.toList());
        }

        for (Category cat : weakCategories) {
            try {
                List<Problem> unsolved = dbManager.getUnsolvedProblems(user.getId(), cat, null);
                unsolved.stream()
                        .sorted(Comparator.comparingDouble(Problem::getAcceptanceRate).reversed())
                        .limit(3)
                        .forEach(recommendations::add);
            } catch (SQLException e) {
                logger.error("Failed to get problems for category {}", cat, e);
            }
        }

        return recommendations.stream()
                .distinct()
                .limit(DEFAULT_RECOMMENDATIONS)
                .collect(Collectors.toList());
    }

    /**
     * Recommends problems at the most appropriate difficulty for the user.
     *
     * <p>Algorithm:
     * <ul>
     *   <li>success rate &lt; 40% → EASY</li>
     *   <li>40% – 70% → MEDIUM</li>
     *   <li>&gt;= 70% → HARD</li>
     * </ul>
     *
     * @param user the user profile
     * @return list of recommended problems at the predicted difficulty
     */
    public List<Problem> recommendByDifficulty(UserProfile user) {
        Statistics stats = analyticsEngine.generateStatistics(user.getId());
        Difficulty targetDifficulty = predictDifficulty(stats.getOverallSuccessRate());

        try {
            List<Problem> unsolved = dbManager.getUnsolvedProblems(user.getId(), null, targetDifficulty);
            return unsolved.stream()
                    .sorted(Comparator.comparingDouble(Problem::getAcceptanceRate).reversed())
                    .limit(DEFAULT_RECOMMENDATIONS)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            logger.error("Failed to recommend by difficulty", e);
            return Collections.emptyList();
        }
    }

    /**
     * Generates a week-by-week learning path for the specified number of weeks.
     *
     * <p>Each week contains 5 problems. Difficulty increases every two weeks.
     * Categories are rotated to ensure diversity.</p>
     *
     * @param user  the user profile
     * @param weeks number of weeks to plan (1–12 recommended)
     * @return ordered list of problems covering the learning path
     */
    public List<Problem> generateLearningPath(UserProfile user, int weeks) {
        Statistics stats = analyticsEngine.generateStatistics(user.getId());
        Difficulty startDifficulty = predictDifficulty(stats.getOverallSuccessRate());
        List<Problem> path = new ArrayList<>();
        Set<Integer> used = new HashSet<>();

        int problemsPerWeek = 5;
        for (int week = 1; week <= weeks; week++) {
            // Escalate difficulty every 2 weeks
            Difficulty difficulty = escalateDifficulty(startDifficulty, (week - 1) / 2);

            // Rotate categories for diversity
            List<Category> categories = getRotatedCategories(user.getId(), week);

            for (Category cat : categories) {
                try {
                    List<Problem> candidates = dbManager.getUnsolvedProblems(user.getId(), cat, difficulty);
                    candidates.stream()
                            .filter(p -> !used.contains(p.getId()))
                            .sorted(Comparator.comparingDouble(Problem::getAcceptanceRate).reversed())
                            .limit(2)
                            .forEach(p -> {
                                used.add(p.getId());
                                path.add(p);
                            });
                } catch (SQLException e) {
                    logger.error("Error building learning path for category {}", cat, e);
                }
                if (path.size() >= week * problemsPerWeek) break;
            }
        }

        return path;
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private Difficulty predictDifficulty(double successRate) {
        if (successRate < 0.4) return Difficulty.EASY;
        if (successRate < 0.7) return Difficulty.MEDIUM;
        return Difficulty.HARD;
    }

    private Difficulty escalateDifficulty(Difficulty start, int steps) {
        int level = Math.min(start.getLevel() - 1 + steps, 2);
        return Difficulty.values()[level];
    }

    private List<Category> getRotatedCategories(int userId, int seed) {
        List<Category> all = Arrays.asList(
                Category.ARRAY, Category.STRING, Category.DYNAMIC_PROGRAMMING,
                Category.GRAPH, Category.TREE, Category.HASH_TABLE,
                Category.TWO_POINTERS, Category.BINARY_SEARCH, Category.BACKTRACKING,
                Category.GREEDY
        );
        Collections.rotate(all, seed % all.size());
        return all;
    }
}
