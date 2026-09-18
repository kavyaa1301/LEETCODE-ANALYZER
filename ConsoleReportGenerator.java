package com.leetcode.analyzer.report;

import com.leetcode.analyzer.model.*;
import com.leetcode.analyzer.util.ConsoleFormatter;

import java.util.Map;

/**
 * Generates formatted reports directly to the console (stdout).
 */
public class ConsoleReportGenerator implements ReportGenerator {

    @Override
    public void generateReport(UserProfile user, Statistics stats, String outputPath) {
        ConsoleFormatter.printBanner("LEETCODE PROGRESS REPORT");
        System.out.printf("%n  User: %s%n", user.getUsername());
        ConsoleFormatter.printSeparator();

        // ── Overall stats ──────────────────────────────────────────────────
        ConsoleFormatter.printHeader("Overall Statistics");
        ConsoleFormatter.printTableRow("Total Solved:", String.valueOf(stats.getTotalSolved()));
        ConsoleFormatter.printTableRow("Total Attempted:", String.valueOf(stats.getTotalAttempted()));
        ConsoleFormatter.printTableRow("Success Rate:",
                String.format("%.1f%%", stats.getOverallSuccessRate() * 100));
        ConsoleFormatter.printTableRow("Avg Attempts per Problem:",
                String.format("%.2f", stats.getAverageAttempts()));
        ConsoleFormatter.printTableRow("Strongest Category:", stats.getStrongestCategory() != null ? stats.getStrongestCategory() : "N/A");
        ConsoleFormatter.printTableRow("Weakest Category:", stats.getWeakestCategory() != null ? stats.getWeakestCategory() : "N/A");

        // ── Difficulty breakdown ───────────────────────────────────────────
        ConsoleFormatter.printHeader("Solved by Difficulty");
        Map<Difficulty, Integer> byDiff = stats.getSolvedByDifficulty();
        if (byDiff != null && !byDiff.isEmpty()) {
            for (Difficulty d : Difficulty.values()) {
                int count = byDiff.getOrDefault(d, 0);
                System.out.printf("  %s%-8s%s : %d%n",
                        ConsoleFormatter.BOLD, d.getDisplayName(), ConsoleFormatter.RESET, count);
            }
        } else {
            ConsoleFormatter.warning("No difficulty data available.");
        }

        // ── Category breakdown ────────────────────────────────────────────
        ConsoleFormatter.printHeader("Performance by Category");
        Map<Category, Double> successRates = stats.getSuccessRateByCategory();
        if (successRates != null && !successRates.isEmpty()) {
            successRates.entrySet().stream()
                .sorted(Map.Entry.<Category, Double>comparingByValue().reversed())
                .forEach(e -> ConsoleFormatter.printProgressBar(
                        e.getKey().getDisplayName(), e.getValue(), 20));
        } else {
            ConsoleFormatter.warning("No category data available.");
        }

        System.out.println();
        ConsoleFormatter.printSeparator();
        ConsoleFormatter.info("Report generated at: " + java.time.LocalDateTime.now());
        System.out.println();
    }
}
