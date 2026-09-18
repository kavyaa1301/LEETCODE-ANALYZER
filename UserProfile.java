package com.leetcode.analyzer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a user's profile including their solution history and statistics.
 */
public class UserProfile {

    private int id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime lastActive;
    private List<Solution> solutionHistory;
    private Statistics statistics;

    /** Default constructor. */
    public UserProfile() {
        this.createdAt = LocalDateTime.now();
        this.lastActive = LocalDateTime.now();
        this.solutionHistory = new ArrayList<>();
        this.statistics = new Statistics();
    }

    /**
     * Constructs a UserProfile with username.
     * @param username the user's display name
     */
    public UserProfile(String username) {
        this();
        this.username = username;
    }

    /**
     * Constructs a full UserProfile.
     * @param username the user's display name
     * @param email    the user's email address
     */
    public UserProfile(String username, String email) {
        this(username);
        this.email = email;
    }

    /** Adds a solution to history and updates lastActive. */
    public void addSolution(Solution solution) {
        if (solution != null) {
            solutionHistory.add(solution);
            this.lastActive = LocalDateTime.now();
        }
    }

    /** Returns count of accepted solutions. */
    public int getTotalSolved() {
        return (int) solutionHistory.stream().filter(Solution::isAccepted).count();
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }

    public List<Solution> getSolutionHistory() { return solutionHistory; }
    public void setSolutionHistory(List<Solution> solutionHistory) {
        this.solutionHistory = solutionHistory != null ? solutionHistory : new ArrayList<>();
    }

    public Statistics getStatistics() { return statistics; }
    public void setStatistics(Statistics statistics) { this.statistics = statistics; }

    // ─── Object overrides ────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile)) return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() { return Objects.hash(username); }

    @Override
    public String toString() {
        return String.format("UserProfile{username='%s', solved=%d}", username, getTotalSolved());
    }
}
