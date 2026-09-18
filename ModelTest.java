package com.leetcode.analyzer.model;

import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Difficulty, Category, Solution, UserProfile, and Statistics models.
 */
@DisplayName("Model Enum and Supporting Class Tests")
class ModelTest {

    // ── Difficulty ─────────────────────────────────────────────────────────────

    @Test @DisplayName("Difficulty.fromString parses correctly")
    void testDifficultyFromString() {
        assertEquals(Difficulty.EASY,   Difficulty.fromString("easy"));
        assertEquals(Difficulty.MEDIUM, Difficulty.fromString("MEDIUM"));
        assertEquals(Difficulty.HARD,   Difficulty.fromString("Hard"));
        assertEquals(Difficulty.MEDIUM, Difficulty.fromString(null));
        assertEquals(Difficulty.MEDIUM, Difficulty.fromString("unknown_value"));
    }

    @Test @DisplayName("Difficulty has correct display names")
    void testDifficultyDisplayNames() {
        assertEquals("Easy",   Difficulty.EASY.getDisplayName());
        assertEquals("Medium", Difficulty.MEDIUM.getDisplayName());
        assertEquals("Hard",   Difficulty.HARD.getDisplayName());
    }

    @Test @DisplayName("Difficulty has increasing level values")
    void testDifficultyLevels() {
        assertTrue(Difficulty.EASY.getLevel() < Difficulty.MEDIUM.getLevel());
        assertTrue(Difficulty.MEDIUM.getLevel() < Difficulty.HARD.getLevel());
    }

    // ── Category ──────────────────────────────────────────────────────────────

    @Test @DisplayName("Category.fromString parses known values")
    void testCategoryFromString() {
        assertEquals(Category.ARRAY,               Category.fromString("Array"));
        assertEquals(Category.DYNAMIC_PROGRAMMING, Category.fromString("Dynamic Programming"));
        assertEquals(Category.GRAPH,               Category.fromString("Graph"));
        assertEquals(Category.UNKNOWN,             Category.fromString(null));
        assertEquals(Category.UNKNOWN,             Category.fromString("ZZZUnknownCategory"));
    }

    @Test @DisplayName("Category display names are human-readable")
    void testCategoryDisplayNames() {
        assertEquals("Dynamic Programming", Category.DYNAMIC_PROGRAMMING.getDisplayName());
        assertEquals("Hash Table",          Category.HASH_TABLE.getDisplayName());
        assertEquals("Binary Search",       Category.BINARY_SEARCH.getDisplayName());
    }

    // ── Solution ──────────────────────────────────────────────────────────────

    @Test @DisplayName("Solution default constructor sets sensible defaults")
    void testSolutionDefaults() {
        Solution s = new Solution();
        assertEquals("Java", s.getLanguage());
        assertEquals(1, s.getAttempts());
        assertNotNull(s.getSolvedAt());
    }

    @Test @DisplayName("Solution parameterized constructor sets all fields")
    void testSolutionConstructor() {
        Solution s = new Solution(42, "code here", "O(n)", "O(1)", "Sliding Window");
        assertEquals(42, s.getProblemId());
        assertEquals("code here", s.getCode());
        assertEquals("O(n)", s.getTimeComplexity());
        assertEquals("O(1)", s.getSpaceComplexity());
        assertEquals("Sliding Window", s.getApproach());
        assertTrue(s.isAccepted());
    }

    @Test @DisplayName("Solution equality based on id")
    void testSolutionEquality() {
        Solution s1 = new Solution(); s1.setId(5);
        Solution s2 = new Solution(); s2.setId(5);
        Solution s3 = new Solution(); s3.setId(6);
        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
    }

    @Test @DisplayName("Solution toString contains key info")
    void testSolutionToString() {
        Solution s = new Solution(1, null, "O(n)", "O(1)", "Brute Force");
        String str = s.toString();
        assertTrue(str.contains("1"));
        assertTrue(str.contains("Brute Force"));
    }

    // ── UserProfile ───────────────────────────────────────────────────────────

    @Test @DisplayName("UserProfile getTotalSolved counts accepted only")
    void testUserProfileTotalSolved() {
        UserProfile user = new UserProfile("tester");
        Solution accepted = new Solution(1, null, "O(n)", "O(1)", "ok");
        accepted.setAccepted(true);
        Solution rejected = new Solution(2, null, "O(n^2)", "O(1)", "slow");
        rejected.setAccepted(false);

        user.addSolution(accepted);
        user.addSolution(rejected);

        assertEquals(1, user.getTotalSolved());
        assertEquals(2, user.getSolutionHistory().size());
    }

    @Test @DisplayName("UserProfile equality based on username")
    void testUserProfileEquality() {
        UserProfile u1 = new UserProfile("alice");
        UserProfile u2 = new UserProfile("alice");
        UserProfile u3 = new UserProfile("bob");
        assertEquals(u1, u2);
        assertNotEquals(u1, u3);
    }

    @Test @DisplayName("addSolution updates lastActive")
    void testAddSolutionUpdatesLastActive() throws InterruptedException {
        UserProfile user = new UserProfile("timer_tester");
        LocalDateTime before = LocalDateTime.now();
        Thread.sleep(10);
        user.addSolution(new Solution(1, null, "O(n)", "O(1)", "test"));
        assertTrue(user.getLastActive().isAfter(before) || user.getLastActive().isEqual(before));
    }

    // ── Statistics ────────────────────────────────────────────────────────────

    @Test @DisplayName("Statistics default constructor initializes maps")
    void testStatisticsDefaults() {
        Statistics s = new Statistics();
        assertNotNull(s.getSolvedByDifficulty());
        assertNotNull(s.getSolvedByCategory());
        assertNotNull(s.getSuccessRateByCategory());
        assertEquals(0, s.getTotalSolved());
        assertEquals(0, s.getTotalAttempted());
    }

    @Test @DisplayName("Statistics toString contains key fields")
    void testStatisticsToString() {
        Statistics s = new Statistics();
        s.setTotalSolved(10);
        s.setTotalAttempted(15);
        String str = s.toString();
        assertTrue(str.contains("10"));
        assertTrue(str.contains("15"));
    }
}
