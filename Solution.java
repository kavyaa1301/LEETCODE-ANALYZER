package com.leetcode.analyzer.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a user's solution to a LeetCode problem.
 *
 * <p>Tracks the code submitted, time/space complexity, the number of attempts,
 * and the approach used to solve the problem.</p>
 */
public class Solution {

    private int id;
    private int problemId;
    private String code;
    private String timeComplexity;
    private String spaceComplexity;
    private int attempts;
    private String approach;
    private boolean accepted;
    private LocalDateTime solvedAt;
    private String notes;
    private String language;

    /** Default constructor. */
    public Solution() {
        this.solvedAt = LocalDateTime.now();
        this.attempts = 1;
        this.language = "Java";
    }

    /**
     * Constructs a Solution with core fields.
     *
     * @param problemId       the associated problem id
     * @param code            the submitted solution code
     * @param timeComplexity  e.g. "O(n)"
     * @param spaceComplexity e.g. "O(1)"
     * @param approach        brief description of the algorithm used
     */
    public Solution(int problemId, String code, String timeComplexity,
                    String spaceComplexity, String approach) {
        this();
        this.problemId = problemId;
        this.code = code;
        this.timeComplexity = timeComplexity;
        this.spaceComplexity = spaceComplexity;
        this.approach = approach;
        this.accepted = true;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTimeComplexity() { return timeComplexity; }
    public void setTimeComplexity(String timeComplexity) { this.timeComplexity = timeComplexity; }

    public String getSpaceComplexity() { return spaceComplexity; }
    public void setSpaceComplexity(String spaceComplexity) { this.spaceComplexity = spaceComplexity; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public String getApproach() { return approach; }
    public void setApproach(String approach) { this.approach = approach; }

    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

    public LocalDateTime getSolvedAt() { return solvedAt; }
    public void setSolvedAt(LocalDateTime solvedAt) { this.solvedAt = solvedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    // ─── Object overrides ────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Solution)) return false;
        Solution solution = (Solution) o;
        return id == solution.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return String.format("Solution{problemId=%d, approach='%s', time=%s, space=%s, accepted=%b}",
                problemId, approach, timeComplexity, spaceComplexity, accepted);
    }
}
