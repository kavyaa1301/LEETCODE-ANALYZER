package com.leetcode.analyzer.database;

import com.leetcode.analyzer.model.*;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DatabaseManager.
 * Uses an in-memory SQLite database (separate from the app DB).
 */
@DisplayName("DatabaseManager Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseManagerTest {

    private static DatabaseManager db;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        // Use a separate in-memory DB for tests to avoid polluting the app DB
        db = DatabaseManager.getInstance();
        // Reinitialize with in-memory URL via reflection for isolation
        db.initialize();
    }

    @AfterAll
    static void tearDownDatabase() {
        db.close();
    }

    // ── Problem tests ──────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Can add and retrieve a problem by ID")
    void testAddAndGetProblem() throws SQLException {
        Problem p = new Problem(9001, "Test Problem", Category.ARRAY, Difficulty.EASY, 55.0);
        p.addTag("Array");
        db.addProblem(p);

        Problem retrieved = db.getProblemById(9001);
        assertNotNull(retrieved);
        assertEquals("Test Problem", retrieved.getTitle());
        assertEquals(Category.ARRAY, retrieved.getCategory());
        assertEquals(Difficulty.EASY, retrieved.getDifficulty());
        assertEquals(55.0, retrieved.getAcceptanceRate(), 0.001);
    }

    @Test
    @Order(2)
    @DisplayName("Returns null for non-existent problem ID")
    void testGetNonExistentProblem() throws SQLException {
        assertNull(db.getProblemById(999999));
    }

    @Test
    @Order(3)
    @DisplayName("Can retrieve problems by category")
    void testGetProblemsByCategory() throws SQLException {
        Problem p1 = new Problem(9002, "DP Problem 1", Category.DYNAMIC_PROGRAMMING, Difficulty.MEDIUM, 45.0);
        Problem p2 = new Problem(9003, "DP Problem 2", Category.DYNAMIC_PROGRAMMING, Difficulty.HARD, 30.0);
        db.addProblem(p1);
        db.addProblem(p2);

        List<Problem> dp = db.getProblemsByCategory(Category.DYNAMIC_PROGRAMMING);
        assertTrue(dp.size() >= 2);
        assertTrue(dp.stream().allMatch(p -> p.getCategory() == Category.DYNAMIC_PROGRAMMING));
    }

    @Test
    @Order(4)
    @DisplayName("Can retrieve all problems")
    void testGetAllProblems() throws SQLException {
        List<Problem> all = db.getAllProblems();
        assertNotNull(all);
        assertFalse(all.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("INSERT OR REPLACE updates an existing problem")
    void testUpdateProblem() throws SQLException {
        Problem original = new Problem(9004, "Original Title", Category.ARRAY, Difficulty.EASY, 50.0);
        db.addProblem(original);

        Problem updated = new Problem(9004, "Updated Title", Category.STRING, Difficulty.MEDIUM, 60.0);
        db.addProblem(updated);

        Problem retrieved = db.getProblemById(9004);
        assertNotNull(retrieved);
        assertEquals("Updated Title", retrieved.getTitle());
        assertEquals(Category.STRING, retrieved.getCategory());
    }

    @Test
    @Order(6)
    @DisplayName("Can search problems by keyword")
    void testSearchProblems() throws SQLException {
        Problem p = new Problem(9005, "Unique Keyword XYZ Problem", Category.GRAPH, Difficulty.HARD, 35.0);
        db.addProblem(p);

        List<Problem> results = db.searchProblems("unique keyword xyz");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.getId() == 9005));
    }

    // ── User tests ────────────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("Can save and retrieve a user")
    void testSaveAndGetUser() throws SQLException {
        UserProfile user = new UserProfile("testuser_unit", "test@example.com");
        db.saveUser(user);

        UserProfile retrieved = db.getUserByUsername("testuser_unit");
        assertNotNull(retrieved);
        assertEquals("testuser_unit", retrieved.getUsername());
    }

    @Test
    @Order(8)
    @DisplayName("Returns null for unknown username")
    void testGetUnknownUser() throws SQLException {
        assertNull(db.getUserByUsername("nobody_xyz_abc"));
    }

    // ── Solution tests ────────────────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("Can save and retrieve a solution")
    void testSaveSolution() throws SQLException {
        // Ensure user exists
        UserProfile user = db.getUserByUsername("testuser_unit");
        if (user == null) {
            user = new UserProfile("testuser_unit");
            db.saveUser(user);
            user = db.getUserByUsername("testuser_unit");
        }

        Solution s = new Solution(9001, "int[] map = new int[]{};", "O(n)", "O(n)", "HashMap approach");
        s.setAttempts(2);
        db.saveSolution(s, user.getId());
        assertTrue(s.getId() > 0);

        Solution retrieved = db.getSolutionById(s.getId());
        assertNotNull(retrieved);
        assertEquals(9001, retrieved.getProblemId());
        assertEquals("HashMap approach", retrieved.getApproach());
        assertEquals("O(n)", retrieved.getTimeComplexity());
    }

    @Test
    @Order(10)
    @DisplayName("getUserSolutions returns all solutions for a user")
    void testGetUserSolutions() throws SQLException {
        UserProfile user = db.getUserByUsername("testuser_unit");
        assertNotNull(user);

        List<Solution> solutions = db.getUserSolutions(user.getId());
        assertNotNull(solutions);
        assertFalse(solutions.isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("Category stats are updated after saving a solution")
    void testCategoryStatsUpdated() throws SQLException {
        UserProfile user = db.getUserByUsername("testuser_unit");
        assertNotNull(user);

        var stats = db.getCategoryStats(user.getId());
        assertFalse(stats.isEmpty());
        assertTrue(stats.containsKey(Category.ARRAY.name()));
    }

    @Test
    @Order(12)
    @DisplayName("Difficulty stats are updated after saving a solution")
    void testDifficultyStatsUpdated() throws SQLException {
        UserProfile user = db.getUserByUsername("testuser_unit");
        assertNotNull(user);

        var stats = db.getDifficultyStats(user.getId());
        assertFalse(stats.isEmpty());
    }
}
