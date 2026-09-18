package com.leetcode.analyzer.cli;

import com.leetcode.analyzer.analytics.AnalyticsEngine;
import com.leetcode.analyzer.database.DatabaseManager;
import com.leetcode.analyzer.model.*;
import com.leetcode.analyzer.parser.CSVParser;
import com.leetcode.analyzer.parser.JSONParser;
import com.leetcode.analyzer.recommendation.RecommendationEngine;
import com.leetcode.analyzer.report.*;
import com.leetcode.analyzer.util.ConsoleFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * Main CLI interface for the LeetCode Analyzer.
 *
 * <p>Provides a menu-driven interface to all application features.</p>
 */
public class CLIInterface {

    private static final Logger logger = LoggerFactory.getLogger(CLIInterface.class);

    private final DatabaseManager dbManager;
    private final AnalyticsEngine analyticsEngine;
    private final RecommendationEngine recommendationEngine;
    private final Scanner scanner;
    private UserProfile currentUser;

    /**
     * Constructs the CLI interface, wiring up all engines.
     *
     * @param dbManager the initialized DatabaseManager singleton
     */
    public CLIInterface(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.analyticsEngine = new AnalyticsEngine(dbManager);
        this.recommendationEngine = new RecommendationEngine(dbManager, analyticsEngine);
        this.scanner = new Scanner(System.in);
    }

    /** Starts the CLI application loop. */
    public void start() {
        ConsoleFormatter.printBanner("LeetCode Problem Analyzer v1.0");
        System.out.println();

        // Load or create user
        loginOrRegister();
        if (currentUser == null) return;

        mainMenu();
    }

    // ─── Login / Register ─────────────────────────────────────────────────────

    private void loginOrRegister() {
        System.out.print(ConsoleFormatter.CYAN + "Enter your username: " + ConsoleFormatter.RESET);
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) { ConsoleFormatter.error("Username cannot be empty."); return; }

        try {
            UserProfile existing = dbManager.getUserByUsername(username);
            if (existing != null) {
                currentUser = existing;
                currentUser.setSolutionHistory(dbManager.getUserSolutions(currentUser.getId()));
                ConsoleFormatter.success("Welcome back, " + username + "!");
            } else {
                currentUser = new UserProfile(username);
                dbManager.saveUser(currentUser);
                // Retrieve generated id
                currentUser = dbManager.getUserByUsername(username);
                ConsoleFormatter.success("New profile created for " + username + "!");
            }
        } catch (SQLException e) {
            ConsoleFormatter.error("Database error: " + e.getMessage());
            logger.error("Login/register failed", e);
        }
    }

    // ─── Main Menu ────────────────────────────────────────────────────────────

    private void mainMenu() {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt();
            System.out.println();
            switch (choice) {
                case 1: addSolutionMenu(); break;
                case 2: viewStatisticsMenu(); break;
                case 3: viewRecommendationsMenu(); break;
                case 4: searchProblemsMenu(); break;
                case 5: viewAnalyticsMenu(); break;
                case 6: generateReportMenu(); break;
                case 7: importDataMenu(); break;
                case 8: running = false; goodbye(); break;
                default: ConsoleFormatter.warning("Invalid option. Please enter 1-8.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println();
        ConsoleFormatter.printSeparator();
        System.out.println(ConsoleFormatter.BOLD + "  Main Menu  [" + currentUser.getUsername() + "]" + ConsoleFormatter.RESET);
        ConsoleFormatter.printSeparator();
        System.out.println("  1. Add Solution");
        System.out.println("  2. View My Statistics");
        System.out.println("  3. Get Recommendations");
        System.out.println("  4. Search Problems");
        System.out.println("  5. View Analytics");
        System.out.println("  6. Generate Report");
        System.out.println("  7. Import Problem Data");
        System.out.println("  8. Exit");
        ConsoleFormatter.printSeparator();
        System.out.print(ConsoleFormatter.CYAN + "  Choose option: " + ConsoleFormatter.RESET);
    }

    // ─── Add Solution ─────────────────────────────────────────────────────────

    private void addSolutionMenu() {
        ConsoleFormatter.printHeader("Add Solution");
        try {
            System.out.print("  Problem ID: ");
            int problemId = readInt();
            Problem problem = dbManager.getProblemById(problemId);
            if (problem == null) {
                ConsoleFormatter.warning("Problem #" + problemId + " not found in database.");
                System.out.print("  Title (for new problem): ");
                String title = scanner.nextLine().trim();
                System.out.print("  Category (Array/String/DP/Graph/etc.): ");
                String cat = scanner.nextLine().trim();
                System.out.print("  Difficulty (Easy/Medium/Hard): ");
                String diff = scanner.nextLine().trim();
                problem = new Problem(problemId, title, Category.fromString(cat), Difficulty.fromString(diff), 0);
                dbManager.addProblem(problem);
                ConsoleFormatter.info("Problem added to database.");
            } else {
                System.out.println("  Found: " + problem);
            }

            System.out.print("  Approach/Notes: ");
            String approach = scanner.nextLine().trim();
            System.out.print("  Time Complexity (e.g. O(n)): ");
            String time = scanner.nextLine().trim();
            System.out.print("  Space Complexity (e.g. O(1)): ");
            String space = scanner.nextLine().trim();
            System.out.print("  Attempts taken: ");
            int attempts = readInt();
            System.out.print("  Was it accepted? (y/n): ");
            boolean accepted = scanner.nextLine().trim().equalsIgnoreCase("y");
            System.out.print("  Code (optional, press Enter to skip): ");
            String code = scanner.nextLine().trim();

            Solution solution = new Solution(problemId, code.isEmpty() ? null : code, time, space, approach);
            solution.setAttempts(attempts);
            solution.setAccepted(accepted);
            dbManager.saveSolution(solution, currentUser.getId());
            currentUser.addSolution(solution);
            ConsoleFormatter.success("Solution saved successfully!");

        } catch (SQLException e) {
            ConsoleFormatter.error("Failed to save solution: " + e.getMessage());
        }
    }

    // ─── Statistics ───────────────────────────────────────────────────────────

    private void viewStatisticsMenu() {
        Statistics stats = analyticsEngine.generateStatistics(currentUser.getId());
        new ConsoleReportGenerator().generateReport(currentUser, stats, null);
        int streak = analyticsEngine.getCurrentStreak(currentUser.getId());
        if (streak > 0) {
            ConsoleFormatter.success("🔥 Current solving streak: " + streak + " day(s)!");
        }
    }

    // ─── Recommendations ──────────────────────────────────────────────────────

    private void viewRecommendationsMenu() {
        ConsoleFormatter.printHeader("Recommendations");
        System.out.println("  1. By Weak Area");
        System.out.println("  2. By Difficulty Level");
        System.out.println("  3. Generate Learning Path");
        System.out.print("  Choose: ");
        int choice = readInt();

        switch (choice) {
            case 1:
                ConsoleFormatter.printHeader("Weak Area Recommendations");
                ConsoleFormatter.printProblemList(recommendationEngine.recommendByWeakArea(currentUser));
                break;
            case 2:
                ConsoleFormatter.printHeader("Difficulty-Based Recommendations");
                ConsoleFormatter.printProblemList(recommendationEngine.recommendByDifficulty(currentUser));
                break;
            case 3:
                System.out.print("  How many weeks? (1-12): ");
                int weeks = readInt();
                weeks = Math.max(1, Math.min(12, weeks));
                ConsoleFormatter.printHeader("Learning Path (" + weeks + " weeks)");
                ConsoleFormatter.printProblemList(recommendationEngine.generateLearningPath(currentUser, weeks));
                break;
            default:
                ConsoleFormatter.warning("Invalid option.");
        }
    }

    // ─── Search ───────────────────────────────────────────────────────────────

    private void searchProblemsMenu() {
        ConsoleFormatter.printHeader("Search Problems");
        System.out.println("  1. Search by keyword");
        System.out.println("  2. Browse by category");
        System.out.println("  3. Browse by difficulty");
        System.out.print("  Choose: ");
        int choice = readInt();

        try {
            switch (choice) {
                case 1:
                    System.out.print("  Enter keyword: ");
                    String kw = scanner.nextLine().trim();
                    ConsoleFormatter.printProblemList(dbManager.searchProblems(kw));
                    break;
                case 2:
                    System.out.println("  Categories: " + Arrays.toString(Category.values()));
                    System.out.print("  Enter category name: ");
                    Category cat = Category.fromString(scanner.nextLine().trim());
                    ConsoleFormatter.printProblemList(dbManager.getProblemsByCategory(cat));
                    break;
                case 3:
                    System.out.print("  Difficulty (Easy/Medium/Hard): ");
                    Difficulty diff = Difficulty.fromString(scanner.nextLine().trim());
                    ConsoleFormatter.printProblemList(dbManager.getProblemsByDifficulty(diff));
                    break;
                default:
                    ConsoleFormatter.warning("Invalid option.");
            }
        } catch (SQLException e) {
            ConsoleFormatter.error("Search failed: " + e.getMessage());
        }
    }

    // ─── Analytics ────────────────────────────────────────────────────────────

    private void viewAnalyticsMenu() {
        ConsoleFormatter.printHeader("Analytics Dashboard");
        Map<Category, Double> weakAreas = analyticsEngine.getWeakAreas(currentUser.getId());
        if (weakAreas.isEmpty()) {
            ConsoleFormatter.info("No analytics data yet. Add some solutions first!");
            return;
        }

        System.out.println(ConsoleFormatter.BOLD + "\n  Category Performance (weakest first):" + ConsoleFormatter.RESET);
        weakAreas.forEach((cat, rate) ->
                ConsoleFormatter.printProgressBar(cat.getDisplayName(), rate, 20));

        System.out.println();
        Map<Difficulty, Integer> diffSummary = analyticsEngine.getDifficultySummary(currentUser.getId());
        ConsoleFormatter.printHeader("Difficulty Distribution");
        diffSummary.forEach((d, count) ->
                ConsoleFormatter.printTableRow(d.getDisplayName(), count + " solved"));

        System.out.println();
        ConsoleFormatter.printHeader("Top Categories (by volume)");
        List<Category> top = analyticsEngine.getTopCategories(currentUser.getId(), 5);
        for (int i = 0; i < top.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, top.get(i).getDisplayName());
        }
    }

    // ─── Reports ──────────────────────────────────────────────────────────────

    private void generateReportMenu() {
        ConsoleFormatter.printHeader("Generate Report");
        System.out.println("  1. Console Report");
        System.out.println("  2. CSV Export");
        System.out.println("  3. PDF Report");
        System.out.print("  Choose: ");
        int choice = readInt();

        Statistics stats = analyticsEngine.generateStatistics(currentUser.getId());
        try {
            switch (choice) {
                case 1:
                    new ConsoleReportGenerator().generateReport(currentUser, stats, null);
                    break;
                case 2:
                    String csvPath = "report_" + currentUser.getUsername() + ".csv";
                    new CSVReportGenerator(dbManager).generateReport(currentUser, stats, csvPath);
                    break;
                case 3:
                    String pdfPath = "report_" + currentUser.getUsername() + ".pdf";
                    new PDFReportGenerator().generateReport(currentUser, stats, pdfPath);
                    break;
                default:
                    ConsoleFormatter.warning("Invalid option.");
            }
        } catch (Exception e) {
            ConsoleFormatter.error("Failed to generate report: " + e.getMessage());
        }
    }

    // ─── Import ───────────────────────────────────────────────────────────────

    private void importDataMenu() {
        ConsoleFormatter.printHeader("Import Problem Data");
        System.out.println("  1. Import from JSON");
        System.out.println("  2. Import from CSV");
        System.out.print("  Choose: ");
        int choice = readInt();

        System.out.print("  File path: ");
        String path = scanner.nextLine().trim();

        try {
            List<Problem> problems;
            if (choice == 1) {
                problems = new JSONParser().parseProblems(path);
            } else if (choice == 2) {
                problems = new CSVParser().parseProblems(path);
            } else {
                ConsoleFormatter.warning("Invalid option.");
                return;
            }

            int imported = 0;
            for (Problem p : problems) {
                try {
                    dbManager.addProblem(p);
                    imported++;
                } catch (SQLException e) {
                    logger.warn("Skipping problem {}: {}", p.getId(), e.getMessage());
                }
            }
            ConsoleFormatter.success("Imported " + imported + " / " + problems.size() + " problems.");
        } catch (Exception e) {
            ConsoleFormatter.error("Import failed: " + e.getMessage());
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private int readInt() {
        try {
            String line = scanner.nextLine().trim();
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void goodbye() {
        System.out.println();
        ConsoleFormatter.printBanner("Thanks for using LeetCode Analyzer!");
        System.out.println();
    }
}
