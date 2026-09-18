package com.leetcode.analyzer.report;

import com.leetcode.analyzer.model.*;
import java.io.IOException;

/**
 * Strategy interface for generating reports in different formats.
 */
public interface ReportGenerator {
    /**
     * Generates a report for the given user.
     * @param user       the user profile
     * @param statistics the user's statistics
     * @param outputPath file path (null for console output)
     * @throws IOException on write error
     */
    void generateReport(UserProfile user, Statistics statistics, String outputPath) throws IOException;
}
