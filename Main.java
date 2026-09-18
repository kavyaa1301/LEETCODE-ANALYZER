package com.leetcode.analyzer;

import com.leetcode.analyzer.cli.CLIInterface;
import com.leetcode.analyzer.database.DatabaseManager;
import com.leetcode.analyzer.parser.JSONParser;
import com.leetcode.analyzer.model.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Entry point for the LeetCode Problem Analyzer application.
 *
 * <p>Initialization order:
 * <ol>
 *   <li>Configure logging</li>
 *   <li>Initialize DatabaseManager singleton</li>
 *   <li>Auto-import sample data if the DB is fresh</li>
 *   <li>Start the CLI interface</li>
 *   <li>Register shutdown hook for cleanup</li>
 * </ol>
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Application entry point.
     *
     * @param args command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        logger.info("Starting LeetCode Problem Analyzer...");

        DatabaseManager dbManager = DatabaseManager.getInstance();

        // Shutdown hook ensures the DB connection is closed cleanly
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            dbManager.close();
            logger.info("Application shut down cleanly.");
        }));

        try {
            dbManager.initialize();
            autoImportSampleData(dbManager);
            new CLIInterface(dbManager).start();
        } catch (SQLException e) {
            logger.error("Fatal database error during startup", e);
            System.err.println("ERROR: Could not initialize database — " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Imports sample problems from {@code data/problems.json} on the first run
     * (when the problems table is empty).
     *
     * @param dbManager the active DatabaseManager
     */
    private static void autoImportSampleData(DatabaseManager dbManager) {
        try {
            if (!dbManager.getAllProblems().isEmpty()) return; // already seeded

            String[] candidates = {"data/problems.json", "problems.json"};
            for (String path : candidates) {
                File f = new File(path);
                if (f.exists()) {
                    logger.info("Auto-importing sample data from {}", path);
                    List<Problem> problems = new JSONParser().parseProblems(path);
                    int count = 0;
                    for (Problem p : problems) {
                        try { dbManager.addProblem(p); count++; }
                        catch (SQLException ignored) {}
                    }
                    System.out.println("[INFO] Auto-imported " + count + " problems from " + path);
                    return;
                }
            }
        } catch (Exception e) {
            logger.warn("Auto-import failed (non-fatal): {}", e.getMessage());
        }
    }
}
