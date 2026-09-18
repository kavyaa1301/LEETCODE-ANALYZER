package com.leetcode.analyzer.analytics;

import com.leetcode.analyzer.database.DatabaseManager;
import com.leetcode.analyzer.model.*;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AnalyticsEngine.
 * Uses the singleton DatabaseManager initialized with a real (file) SQLite DB.
 */
@DisplayName("AnalyticsEngine Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnalyticsEngineTest {

    private static DatabaseManager db;
    private static AnalyticsEngine engine;
    private static UserProfile user;

    @BeforeAll
    static void setUp() throws SQLException {
        db = DatabaseManager.getInstance();
        db.initialize();
        engine = new AnalyticsEngine(db);

        // Create test user
        user = new UserProfile("analytics_test_user");
        db.saveUser(user);
        user = db.getUserByUsername("analytics_test_user");
        assertNotNull(user, "User should be persisted");

        // Seed some problems and solutions
        seedData();
    }

    @AfterAll
    static void tearDown() {
        db.close();
    }

    private static void seedData() throws SQLException {
        // Add problems in different categories
        Problem arrayProblem  = new Problem(8001, "Array Test 1", Category.ARRAY, Difficulty.EASY, 60.0);
        Problem arrayProblem2 = new Problem(8002, "Array Test 2", Category.ARRAY, Difficulty.MEDIUM, 45.0);
        Problem dpProblem     = new Problem(8003, "DP Test 1", Category.DYNAMIC_PROGRAMMING, Difficulty.MEDIUM, 40.0);
        Problem graphProblem  = new Problem(8004, "Graph Test 1", Category.GRAPH, Difficulty.HARD, 30.0);
        db.addProblem(arrayProblem);
        db.addProblem(arrayProblem2);
        db.addProblem(dpProblem);
        db.addProblem(graphProblem);

        // Add solutions — 2 accepted for Array, 1 for DP, 0 accepted for Graph
        Solution s1 = new Solution(8001, null, "O(n)", "O(1)", "Two pointer");
        s1.setAccepted(true);
        db.saveSolution(s1, user.getId());

        Solution s2 = new Solution(8002, null, "O(n)", "O(n)", "HashMap");
        s2.setAccepted(true);
        db.saveSolution(s2, user.getId());

        Solution s3 = new Solution(8003, null, "O(n^2)", "O(n)", "Memoization");
        s3.setAccepted(true);
        db.saveSolution(s3, user.getId());

        Solution s4 = new Solution(8004, null, "O(V+E)", "O(V)", "BFS");
        s4.setAccepted(false);
        db.saveSolution(s4, user.getId());
    }

    @Test
    @Order(1)
    @DisplayName("generateStatistics returns non-null Statistics")
    void testGenerateStatisticsNotNull() {
        Statistics stats = engine.generateStatistics(user.getId());
        assertNotNull(stats);
    }

    @Test
    @Order(2)
    @DisplayName("generateStatistics counts solved correctly")
    void testGenerateStatisticsSolvedCount() {
        Statistics stats = engine.generateStatistics(user.getId());
        // 3 accepted out of 4 attempted
        assertTrue(stats.getTotalSolved() >= 3);
        assertTrue(stats.getTotalAttempted() >= 4);
    }

    @Test
    @Order(3)
    @DisplayName("generateStatistics calculates success rate correctly")
    void testGenerateStatisticsSuccessRate() {
        Statistics stats = engine.generateStatistics(user.getId());
        // At least 3/4 = 0.75 success rate from seeded data
        assertTrue(stats.getOverallSuccessRate() > 0.0);
        assertTrue(stats.getOverallSuccessRate() <= 1.0);
    }

    @Test
    @Order(4)
    @DisplayName("getWeakAreas returns sorted map, weakest first")
    void testGetWeakAreas() {
        Map<Category, Double> weak = engine.getWeakAreas(user.getId());
        assertNotNull(weak);
        assertFalse(weak.isEmpty());

        // Verify ascending order (weakest first)
        double prev = -1;
        for (double rate : weak.values()) {
            assertTrue(rate >= prev, "Weak areas should be sorted ascending");
            prev = rate;
        }
    }

    @Test
    @Order(5)
    @DisplayName("calculateSuccessRate returns 0 for category with no data")
    void testCalculateSuccessRateNoData() {
        double rate = engine.calculateSuccessRate(user.getId(), Category.RECURSION);
        assertEquals(0.0, rate, 0.001);
    }

    @Test
    @Order(6)
    @DisplayName("calculateSuccessRate returns correct value for known category")
    void testCalculateSuccessRateKnown() {
        // Array: 2 solved / 2 attempted = 1.0
        double rate = engine.calculateSuccessRate(user.getId(), Category.ARRAY);
        assertTrue(rate > 0.0);
    }

    @Test
    @Order(7)
    @DisplayName("getDifficultySummary returns all difficulty levels")
    void testGetDifficultySummary() {
        Map<Difficulty, Integer> summary = engine.getDifficultySummary(user.getId());
        assertNotNull(summary);
        // Should have all three difficulty keys
        for (Difficulty d : Difficulty.values()) {
            assertTrue(summary.containsKey(d), "Missing difficulty: " + d);
        }
    }

    @Test
    @Order(8)
    @DisplayName("getTopCategories returns at most limit items")
    void testGetTopCategories() {
        List<Category> top = engine.getTopCategories(user.getId(), 2);
        assertNotNull(top);
        assertTrue(top.size() <= 2);
    }

    @Test
    @Order(9)
    @DisplayName("getProgressOverTime returns correct date range")
    void testGetProgressOverTime() {
        Map<LocalDate, Long> progress = engine.getProgressOverTime(
                user.getId(),
                LocalDate.now().minusDays(30),
                LocalDate.now());
        assertNotNull(progress);
        // All dates should be in range
        progress.keySet().forEach(date -> {
            assertFalse(date.isBefore(LocalDate.now().minusDays(30)));
            assertFalse(date.isAfter(LocalDate.now()));
        });
    }

    @Test
    @Order(10)
    @DisplayName("getCurrentStreak returns non-negative value")
    void testGetCurrentStreak() {
        int streak = engine.getCurrentStreak(user.getId());
        assertTrue(streak >= 0);
    }

    @Test
    @Order(11)
    @DisplayName("generateStatistics identifies strongest and weakest categories")
    void testStrongestWeakest() {
        Statistics stats = engine.generateStatistics(user.getId());
        // After seeding data these should not be null
        assertNotNull(stats.getStrongestCategory());
        assertNotNull(stats.getWeakestCategory());
    }
}
