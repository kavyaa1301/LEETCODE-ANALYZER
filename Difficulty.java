package com.leetcode.analyzer.model;

/**
 * Represents the difficulty level of a LeetCode problem.
 * Each difficulty has an associated numeric value for calculations.
 */
public enum Difficulty {
    EASY(1, "Easy"),
    MEDIUM(2, "Medium"),
    HARD(3, "Hard");

    private final int level;
    private final String displayName;

    Difficulty(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() { return level; }
    public String getDisplayName() { return displayName; }

    /**
     * Parse a string to Difficulty, case-insensitive.
     * @param value the string to parse
     * @return the matching Difficulty, defaults to MEDIUM if unknown
     */
    public static Difficulty fromString(String value) {
        if (value == null) return MEDIUM;
        switch (value.trim().toUpperCase()) {
            case "EASY": return EASY;
            case "HARD": return HARD;
            default: return MEDIUM;
        }
    }

    @Override
    public String toString() { return displayName; }
}
