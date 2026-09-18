package com.leetcode.analyzer.util;

import com.leetcode.analyzer.model.*;
import java.util.List;
import java.util.Map;

/**
 * Utility class for formatting CLI output with tables, colors, and progress bars.
 */
public class ConsoleFormatter {

    // ANSI color codes
    public static final String RESET   = "\u001B[0m";
    public static final String BOLD    = "\u001B[1m";
    public static final String RED     = "\u001B[31m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String BLUE    = "\u001B[34m";
    public static final String CYAN    = "\u001B[36m";
    public static final String WHITE   = "\u001B[37m";
    public static final String BRIGHT_GREEN  = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_RED    = "\u001B[91m";

    private ConsoleFormatter() {}

    /** Prints a centered, bold banner. */
    public static void printBanner(String title) {
        int width = 60;
        String border = "═".repeat(width);
        System.out.println(CYAN + "╔" + border + "╗" + RESET);
        System.out.println(CYAN + "║" + center(title, width) + "║" + RESET);
        System.out.println(CYAN + "╚" + border + "╝" + RESET);
    }

    /** Prints a separator line. */
    public static void printSeparator() {
        System.out.println(CYAN + "─".repeat(62) + RESET);
    }

    /** Prints a section header. */
    public static void printHeader(String header) {
        System.out.println();
        System.out.println(BOLD + BLUE + "▶ " + header + RESET);
        System.out.println(BLUE + "─".repeat(header.length() + 2) + RESET);
    }

    /** Centers text within a given width using spaces. */
    public static String center(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }

    /**
     * Renders an ASCII progress bar.
     *
     * @param label   the label to show before the bar
     * @param value   current value (0.0 – 1.0)
     * @param width   width of the bar in characters
     */
    public static void printProgressBar(String label, double value, int width) {
        int filled = (int) (value * width);
        String bar = "█".repeat(filled) + "░".repeat(width - filled);
        String color = value < 0.4 ? RED : value < 0.7 ? YELLOW : GREEN;
        System.out.printf("%-25s [%s%s%s] %5.1f%%%n",
                label, color, bar, RESET, value * 100);
    }

    /**
     * Prints a two-column table row.
     * @param key   left column
     * @param value right column
     */
    public static void printTableRow(String key, String value) {
        System.out.printf("  %-28s %s%s%s%n", key, WHITE, value, RESET);
    }

    /** Formats a difficulty label with color. */
    public static String colorDifficulty(Difficulty d) {
        switch (d) {
            case EASY:   return GREEN  + "Easy"   + RESET;
            case MEDIUM: return YELLOW + "Medium" + RESET;
            case HARD:   return RED    + "Hard"   + RESET;
            default:     return d.getDisplayName();
        }
    }

    /**
     * Prints a numbered list of problems as a formatted table.
     * @param problems the list to display
     */
    public static void printProblemList(List<Problem> problems) {
        if (problems.isEmpty()) {
            System.out.println(YELLOW + "  No problems found." + RESET);
            return;
        }
        System.out.printf("  %-5s %-50s %-20s %-8s %-6s%n", "#", "Title", "Category", "Diff", "Rate%");
        printSeparator();
        for (int i = 0; i < problems.size(); i++) {
            Problem p = problems.get(i);
            System.out.printf("  %-5d %-50s %-20s %-15s %.1f%%%n",
                    i + 1,
                    truncate(p.getTitle(), 49),
                    p.getCategory().getDisplayName(),
                    colorDifficulty(p.getDifficulty()),
                    p.getAcceptanceRate());
        }
    }

    private static String truncate(String s, int maxLen) {
        return s.length() > maxLen ? s.substring(0, maxLen - 2) + ".." : s;
    }

    /** Prints a success message in green. */
    public static void success(String message) {
        System.out.println(GREEN + "✔ " + message + RESET);
    }

    /** Prints an error message in red. */
    public static void error(String message) {
        System.out.println(RED + "✖ " + message + RESET);
    }

    /** Prints a warning in yellow. */
    public static void warning(String message) {
        System.out.println(YELLOW + "⚠ " + message + RESET);
    }

    /** Prints an info message in cyan. */
    public static void info(String message) {
        System.out.println(CYAN + "ℹ " + message + RESET);
    }
}
