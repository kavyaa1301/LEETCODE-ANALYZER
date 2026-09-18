package com.leetcode.analyzer.model;

import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Problem model class.
 */
@DisplayName("Problem Model Tests")
class ProblemTest {

    private Problem problem;

    @BeforeEach
    void setUp() {
        problem = new Problem(1, "Two Sum", Category.ARRAY, Difficulty.EASY, 49.1);
    }

    @Test
    @DisplayName("Default constructor creates empty problem")
    void testDefaultConstructor() {
        Problem p = new Problem();
        assertEquals(0, p.getId());
        assertNull(p.getTitle());
        assertNotNull(p.getTags());
        assertTrue(p.getTags().isEmpty());
    }

    @Test
    @DisplayName("Parameterized constructor sets all fields")
    void testParameterizedConstructor() {
        assertEquals(1, problem.getId());
        assertEquals("Two Sum", problem.getTitle());
        assertEquals(Category.ARRAY, problem.getCategory());
        assertEquals(Difficulty.EASY, problem.getDifficulty());
        assertEquals(49.1, problem.getAcceptanceRate(), 0.001);
    }

    @Test
    @DisplayName("Tags can be added and are unique")
    void testAddTag() {
        problem.addTag("Hash Table");
        problem.addTag("Array");
        problem.addTag("Array"); // duplicate — should not be added again
        List<String> tags = problem.getTags();
        assertEquals(2, tags.size());
        assertTrue(tags.contains("Hash Table"));
        assertTrue(tags.contains("Array"));
    }

    @Test
    @DisplayName("Null tag is ignored")
    void testAddNullTag() {
        problem.addTag(null);
        assertTrue(problem.getTags().isEmpty());
    }

    @Test
    @DisplayName("Equality is based on id only")
    void testEquality() {
        Problem same = new Problem(1, "Two Sum (different title)", Category.HASH_TABLE, Difficulty.HARD, 10.0);
        Problem different = new Problem(2, "Add Two Numbers", Category.LINKED_LIST, Difficulty.MEDIUM, 40.0);
        assertEquals(problem, same);
        assertNotEquals(problem, different);
    }

    @Test
    @DisplayName("HashCode is consistent with equals")
    void testHashCode() {
        Problem same = new Problem(1, "Whatever", Category.ARRAY, Difficulty.EASY, 0);
        assertEquals(problem.hashCode(), same.hashCode());
    }

    @Test
    @DisplayName("toString contains key info")
    void testToString() {
        String s = problem.toString();
        assertTrue(s.contains("1"));
        assertTrue(s.contains("Two Sum"));
        assertTrue(s.contains("Easy"));
    }

    @Test
    @DisplayName("Setters work correctly")
    void testSetters() {
        problem.setId(99);
        problem.setTitle("Updated");
        problem.setCategory(Category.DYNAMIC_PROGRAMMING);
        problem.setDifficulty(Difficulty.HARD);
        problem.setAcceptanceRate(25.5);
        problem.setCompaniesCount(10);

        assertEquals(99, problem.getId());
        assertEquals("Updated", problem.getTitle());
        assertEquals(Category.DYNAMIC_PROGRAMMING, problem.getCategory());
        assertEquals(Difficulty.HARD, problem.getDifficulty());
        assertEquals(25.5, problem.getAcceptanceRate(), 0.001);
        assertEquals(10, problem.getCompaniesCount());
    }

    @Test
    @DisplayName("setTags replaces list and handles null")
    void testSetTags() {
        problem.setTags(null);
        assertNotNull(problem.getTags());
        assertTrue(problem.getTags().isEmpty());
    }
}
