package com.leetcode.analyzer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds aggregated statistics for a user's LeetCode progress.
 */
public class Statistics {

    private int totalSolved;
    private int totalAttempted;
    private double overallSuccessRate;
    private Map<Difficulty, Integer> solvedByDifficulty;
    private Map<Category, Integer> solvedByCategory;
    private Map<Category, Double> successRateByCategory;
    private double averageAttempts;
    private String strongestCategory;
    private String weakestCategory;

    public Statistics() {
        this.solvedByDifficulty = new HashMap<>();
        this.solvedByCategory = new HashMap<>();
        this.successRateByCategory = new HashMap<>();
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int getTotalSolved() { return totalSolved; }
    public void setTotalSolved(int totalSolved) { this.totalSolved = totalSolved; }

    public int getTotalAttempted() { return totalAttempted; }
    public void setTotalAttempted(int totalAttempted) { this.totalAttempted = totalAttempted; }

    public double getOverallSuccessRate() { return overallSuccessRate; }
    public void setOverallSuccessRate(double overallSuccessRate) { this.overallSuccessRate = overallSuccessRate; }

    public Map<Difficulty, Integer> getSolvedByDifficulty() { return solvedByDifficulty; }
    public void setSolvedByDifficulty(Map<Difficulty, Integer> solvedByDifficulty) { this.solvedByDifficulty = solvedByDifficulty; }

    public Map<Category, Integer> getSolvedByCategory() { return solvedByCategory; }
    public void setSolvedByCategory(Map<Category, Integer> solvedByCategory) { this.solvedByCategory = solvedByCategory; }

    public Map<Category, Double> getSuccessRateByCategory() { return successRateByCategory; }
    public void setSuccessRateByCategory(Map<Category, Double> successRateByCategory) { this.successRateByCategory = successRateByCategory; }

    public double getAverageAttempts() { return averageAttempts; }
    public void setAverageAttempts(double averageAttempts) { this.averageAttempts = averageAttempts; }

    public String getStrongestCategory() { return strongestCategory; }
    public void setStrongestCategory(String strongestCategory) { this.strongestCategory = strongestCategory; }

    public String getWeakestCategory() { return weakestCategory; }
    public void setWeakestCategory(String weakestCategory) { this.weakestCategory = weakestCategory; }

    @Override
    public String toString() {
        return String.format("Statistics{totalSolved=%d, totalAttempted=%d, successRate=%.1f%%, weakest='%s', strongest='%s'}",
                totalSolved, totalAttempted, overallSuccessRate * 100, weakestCategory, strongestCategory);
    }
}
