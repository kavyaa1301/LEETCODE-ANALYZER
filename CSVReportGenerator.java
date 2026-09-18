package com.leetcode.analyzer.report;

import com.leetcode.analyzer.database.DatabaseManager;
import com.leetcode.analyzer.model.*;
import org.apache.commons.csv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * Exports user solutions and statistics to a CSV file.
 */
public class CSVReportGenerator implements ReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CSVReportGenerator.class);
    private final DatabaseManager dbManager;

    public CSVReportGenerator(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void generateReport(UserProfile user, Statistics stats, String outputPath) throws IOException {
        String path = (outputPath != null) ? outputPath : "report_" + user.getUsername() + ".csv";
        logger.info("Generating CSV report to {}", path);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Solution ID", "Problem ID", "Approach",
                                 "Language", "Time Complexity", "Space Complexity",
                                 "Attempts", "Accepted", "Solved At", "Notes"))) {

            List<Solution> solutions = dbManager.getUserSolutions(user.getId());
            for (Solution s : solutions) {
                printer.printRecord(
                        s.getId(), s.getProblemId(), s.getApproach(),
                        s.getLanguage(), s.getTimeComplexity(), s.getSpaceComplexity(),
                        s.getAttempts(), s.isAccepted() ? "Yes" : "No",
                        s.getSolvedAt(), s.getNotes()
                );
            }

            // Summary rows
            printer.println();
            printer.printRecord("=== SUMMARY ===");
            printer.printRecord("Total Solved", stats.getTotalSolved());
            printer.printRecord("Total Attempted", stats.getTotalAttempted());
            printer.printRecord("Success Rate", String.format("%.1f%%", stats.getOverallSuccessRate() * 100));
            printer.printRecord("Strongest Category", stats.getStrongestCategory());
            printer.printRecord("Weakest Category", stats.getWeakestCategory());

        } catch (SQLException e) {
            throw new IOException("Failed to retrieve solutions from database", e);
        }

        logger.info("CSV report saved to {}", path);
        System.out.println("CSV report saved to: " + path);
    }
}
