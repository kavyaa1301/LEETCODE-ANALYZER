package com.leetcode.analyzer.model;

/**
 * Represents problem categories/topics on LeetCode.
 */
public enum Category {
    ARRAY("Array"),
    DYNAMIC_PROGRAMMING("Dynamic Programming"),
    GRAPH("Graph"),
    STRING("String"),
    HASH_TABLE("Hash Table"),
    BACKTRACKING("Backtracking"),
    TREE("Tree"),
    BINARY_SEARCH("Binary Search"),
    TWO_POINTERS("Two Pointers"),
    SLIDING_WINDOW("Sliding Window"),
    LINKED_LIST("Linked List"),
    STACK("Stack"),
    QUEUE("Queue"),
    HEAP("Heap"),
    GREEDY("Greedy"),
    MATH("Math"),
    BIT_MANIPULATION("Bit Manipulation"),
    SORTING("Sorting"),
    RECURSION("Recursion"),
    DIVIDE_AND_CONQUER("Divide and Conquer"),
    UNKNOWN("Unknown");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    /**
     * Parse a string to Category, case-insensitive.
     * @param value the string to parse
     * @return matching Category or UNKNOWN
     */
    public static Category fromString(String value) {
        if (value == null) return UNKNOWN;
        String normalized = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        try {
            return Category.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Try matching by display name
            for (Category cat : values()) {
                if (cat.displayName.equalsIgnoreCase(value.trim())) return cat;
            }
            return UNKNOWN;
        }
    }

    @Override
    public String toString() { return displayName; }
}
