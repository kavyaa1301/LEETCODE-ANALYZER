package com.leetcode.analyzer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a LeetCode problem with all its metadata.
 * 
 * <p>Example usage:
 * <pre>
 *   Problem p = new Problem(1, "Two Sum", Category.ARRAY, Difficulty.EASY, 48.2);
 *   p.addTag("Hash Table");
 * </pre>
 */
public class Problem {

    private int id;
    private String title;
    private Category category;
    private Difficulty difficulty;
    private double acceptanceRate;
    private int companiesCount;
    private List<String> tags;
    private String url;

    /** Default constructor required for frameworks. */
    public Problem() {
        this.tags = new ArrayList<>();
    }

    /**
     * Constructs a Problem with all core fields.
     *
     * @param id             the LeetCode problem number
     * @param title          the problem title
     * @param category       the primary category
     * @param difficulty     the difficulty level
     * @param acceptanceRate the percentage of accepted submissions
     */
    public Problem(int id, String title, Category category, Difficulty difficulty, double acceptanceRate) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.difficulty = difficulty;
        this.acceptanceRate = acceptanceRate;
        this.tags = new ArrayList<>();
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public double getAcceptanceRate() { return acceptanceRate; }
    public void setAcceptanceRate(double acceptanceRate) { this.acceptanceRate = acceptanceRate; }

    public int getCompaniesCount() { return companiesCount; }
    public void setCompaniesCount(int companiesCount) { this.companiesCount = companiesCount; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    /** Adds a tag if not already present. */
    public void addTag(String tag) {
        if (tag != null && !tags.contains(tag)) tags.add(tag);
    }

    // ─── Object overrides ────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Problem)) return false;
        Problem problem = (Problem) o;
        return id == problem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | %.1f%%",
                id, title, category.getDisplayName(), difficulty.getDisplayName(), acceptanceRate);
    }
}
