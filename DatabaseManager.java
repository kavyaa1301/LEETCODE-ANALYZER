package com.leetcode.analyzer.database;

import com.leetcode.analyzer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton database manager for all SQLite operations.
 *
 * <p>Uses a single connection with prepared statements for security.
 * Always call {@link #initialize()} before any other method.</p>
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_URL = "jdbc:sqlite:leetcode_analyzer.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {}

    /**
     * Returns the singleton instance, creating it if necessary.
     * @return the DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Opens the database connection and creates all tables if they don't exist.
     * @throws SQLException if the connection cannot be established
     */
    public void initialize() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        connection.setAutoCommit(true);
        createTables();
        logger.info("Database initialized at {}", DB_URL);
    }

    /** Returns the active connection, throwing if not initialized. */
    public Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("Database not initialized. Call initialize() first.");
        }
        return connection;
    }

    /** Closes the database connection. */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed.");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }

    // ─── Schema creation ──────────────────────────────────────────────────────

    private void createTables() throws SQLException {
        String[] ddl = {
            // Problems table
            "CREATE TABLE IF NOT EXISTS problems (" +
            "  id INTEGER PRIMARY KEY," +
            "  title TEXT NOT NULL," +
            "  category TEXT NOT NULL," +
            "  difficulty TEXT NOT NULL," +
            "  acceptance_rate REAL DEFAULT 0.0," +
            "  companies_count INTEGER DEFAULT 0," +
            "  tags TEXT," +
            "  url TEXT" +
            ")",

            // Users table
            "CREATE TABLE IF NOT EXISTS users (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  username TEXT UNIQUE NOT NULL," +
            "  email TEXT," +
            "  created_at TEXT NOT NULL," +
            "  last_active TEXT" +
            ")",

            // Solutions table
            "CREATE TABLE IF NOT EXISTS solutions (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  problem_id INTEGER NOT NULL," +
            "  user_id INTEGER NOT NULL," +
            "  code TEXT," +
            "  time_complexity TEXT," +
            "  space_complexity TEXT," +
            "  attempts INTEGER DEFAULT 1," +
            "  approach TEXT," +
            "  accepted INTEGER DEFAULT 1," +
            "  solved_at TEXT," +
            "  notes TEXT," +
            "  language TEXT DEFAULT 'Java'," +
            "  FOREIGN KEY(problem_id) REFERENCES problems(id)," +
            "  FOREIGN KEY(user_id) REFERENCES users(id)" +
            ")",

            // Category stats
            "CREATE TABLE IF NOT EXISTS category_stats (" +
            "  user_id INTEGER NOT NULL," +
            "  category TEXT NOT NULL," +
            "  solved_count INTEGER DEFAULT 0," +
            "  attempted_count INTEGER DEFAULT 0," +
            "  PRIMARY KEY(user_id, category)" +
            ")",

            // Difficulty stats
            "CREATE TABLE IF NOT EXISTS difficulty_stats (" +
            "  user_id INTEGER NOT NULL," +
            "  difficulty TEXT NOT NULL," +
            "  solved_count INTEGER DEFAULT 0," +
            "  PRIMARY KEY(user_id, difficulty)" +
            ")"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : ddl) {
                stmt.execute(sql);
            }
        }
        logger.debug("All tables created/verified.");
    }

    // ─── Problem CRUD ─────────────────────────────────────────────────────────

    /**
     * Inserts or replaces a problem in the database.
     * @param problem the problem to save
     * @throws SQLException on database error
     */
    public void addProblem(Problem problem) throws SQLException {
        String sql = "INSERT OR REPLACE INTO problems " +
                     "(id, title, category, difficulty, acceptance_rate, companies_count, tags, url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, problem.getId());
            ps.setString(2, problem.getTitle());
            ps.setString(3, problem.getCategory().name());
            ps.setString(4, problem.getDifficulty().name());
            ps.setDouble(5, problem.getAcceptanceRate());
            ps.setInt(6, problem.getCompaniesCount());
            ps.setString(7, String.join(",", problem.getTags()));
            ps.setString(8, problem.getUrl());
            ps.executeUpdate();
        }
        logger.debug("Saved problem: {}", problem.getTitle());
    }

    /**
     * Retrieves a problem by its ID.
     * @param id the problem number
     * @return the Problem, or null if not found
     * @throws SQLException on database error
     */
    public Problem getProblemById(int id) throws SQLException {
        String sql = "SELECT * FROM problems WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapProblem(rs);
            }
        }
        return null;
    }

    /**
     * Returns all problems in the given category.
     * @param category the category to filter by
     * @return list of matching problems
     * @throws SQLException on database error
     */
    public List<Problem> getProblemsByCategory(Category category) throws SQLException {
        String sql = "SELECT * FROM problems WHERE category = ? ORDER BY acceptance_rate DESC";
        List<Problem> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapProblem(rs));
            }
        }
        return result;
    }

    /**
     * Returns all problems in the given difficulty.
     * @param difficulty the difficulty to filter by
     * @return list of matching problems
     * @throws SQLException on database error
     */
    public List<Problem> getProblemsByDifficulty(Difficulty difficulty) throws SQLException {
        String sql = "SELECT * FROM problems WHERE difficulty = ? ORDER BY acceptance_rate DESC";
        List<Problem> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, difficulty.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapProblem(rs));
            }
        }
        return result;
    }

    /**
     * Returns all problems in the database.
     * @return list of all problems
     * @throws SQLException on database error
     */
    public List<Problem> getAllProblems() throws SQLException {
        String sql = "SELECT * FROM problems ORDER BY id";
        List<Problem> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapProblem(rs));
        }
        return result;
    }

    /**
     * Searches problems by keyword in title.
     * @param keyword the search term
     * @return matching problems
     * @throws SQLException on database error
     */
    public List<Problem> searchProblems(String keyword) throws SQLException {
        String sql = "SELECT * FROM problems WHERE LOWER(title) LIKE ? ORDER BY id";
        List<Problem> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapProblem(rs));
            }
        }
        return result;
    }

    private Problem mapProblem(ResultSet rs) throws SQLException {
        Problem p = new Problem();
        p.setId(rs.getInt("id"));
        p.setTitle(rs.getString("title"));
        p.setCategory(Category.valueOf(rs.getString("category")));
        p.setDifficulty(Difficulty.valueOf(rs.getString("difficulty")));
        p.setAcceptanceRate(rs.getDouble("acceptance_rate"));
        p.setCompaniesCount(rs.getInt("companies_count"));
        String tags = rs.getString("tags");
        if (tags != null && !tags.isEmpty()) {
            for (String t : tags.split(",")) p.addTag(t.trim());
        }
        p.setUrl(rs.getString("url"));
        return p;
    }

    // ─── User CRUD ────────────────────────────────────────────────────────────

    /**
     * Creates or updates a user profile.
     * @param user the user to save
     * @throws SQLException on database error
     */
    public void saveUser(UserProfile user) throws SQLException {
        String sql = "INSERT OR REPLACE INTO users (id, username, email, created_at, last_active) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (user.getId() > 0) ps.setInt(1, user.getId()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getCreatedAt().toString());
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();

            // Retrieve generated id
            try (Statement idStmt = connection.createStatement();
                 ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next() && user.getId() == 0) user.setId(rs.getInt(1));
            }
        }
    }

    /**
     * Loads a user by username.
     * @param username the username to look up
     * @return the UserProfile or null
     * @throws SQLException on database error
     */
    public UserProfile getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserProfile u = new UserProfile();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setEmail(rs.getString("email"));
                    return u;
                }
            }
        }
        return null;
    }

    // ─── Solution CRUD ────────────────────────────────────────────────────────

    /**
     * Saves a solution and updates category/difficulty stats.
     * @param solution the solution to save
     * @param userId   the id of the submitting user
     * @throws SQLException on database error
     */
    public void saveSolution(Solution solution, int userId) throws SQLException {
        String sql = "INSERT INTO solutions " +
                     "(problem_id, user_id, code, time_complexity, space_complexity, " +
                     "attempts, approach, accepted, solved_at, notes, language) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, solution.getProblemId());
            ps.setInt(2, userId);
            ps.setString(3, solution.getCode());
            ps.setString(4, solution.getTimeComplexity());
            ps.setString(5, solution.getSpaceComplexity());
            ps.setInt(6, solution.getAttempts());
            ps.setString(7, solution.getApproach());
            ps.setInt(8, solution.isAccepted() ? 1 : 0);
            ps.setString(9, (solution.getSolvedAt() != null ? solution.getSolvedAt() : LocalDateTime.now()).toString());
            ps.setString(10, solution.getNotes());
            ps.setString(11, solution.getLanguage());
            ps.executeUpdate();

            try (Statement idStmt = connection.createStatement();
                 ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) solution.setId(rs.getInt(1));
            }
        }

        // Update category & difficulty stats
        Problem problem = getProblemById(solution.getProblemId());
        if (problem != null) {
            updateCategoryStats(userId, problem.getCategory(), solution.isAccepted());
            updateDifficultyStats(userId, problem.getDifficulty());
        }
        logger.debug("Saved solution for problemId={}", solution.getProblemId());
    }

    /**
     * Returns a solution by its id.
     * @param solutionId the solution id
     * @return the Solution or null
     * @throws SQLException on database error
     */
    public Solution getSolutionById(int solutionId) throws SQLException {
        String sql = "SELECT * FROM solutions WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, solutionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSolution(rs);
            }
        }
        return null;
    }

    /**
     * Returns all solutions for a given user.
     * @param userId the user's id
     * @return list of the user's solutions
     * @throws SQLException on database error
     */
    public List<Solution> getUserSolutions(int userId) throws SQLException {
        String sql = "SELECT * FROM solutions WHERE user_id = ? ORDER BY solved_at DESC";
        List<Solution> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapSolution(rs));
            }
        }
        return result;
    }

    private Solution mapSolution(ResultSet rs) throws SQLException {
        Solution s = new Solution();
        s.setId(rs.getInt("id"));
        s.setProblemId(rs.getInt("problem_id"));
        s.setCode(rs.getString("code"));
        s.setTimeComplexity(rs.getString("time_complexity"));
        s.setSpaceComplexity(rs.getString("space_complexity"));
        s.setAttempts(rs.getInt("attempts"));
        s.setApproach(rs.getString("approach"));
        s.setAccepted(rs.getInt("accepted") == 1);
        String solvedAt = rs.getString("solved_at");
        if (solvedAt != null) s.setSolvedAt(LocalDateTime.parse(solvedAt));
        s.setNotes(rs.getString("notes"));
        s.setLanguage(rs.getString("language"));
        return s;
    }

    // ─── Stats helpers ────────────────────────────────────────────────────────

    private void updateCategoryStats(int userId, Category category, boolean accepted) throws SQLException {
        // Read existing counts first
        int existingSolved = 0, existingAttempted = 0;
        String select = "SELECT solved_count, attempted_count FROM category_stats " +
                "WHERE user_id = ? AND category = ?";
        try (PreparedStatement ps = connection.prepareStatement(select)) {
            ps.setInt(1, userId);
            ps.setString(2, category.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    existingSolved    = rs.getInt("solved_count");
                    existingAttempted = rs.getInt("attempted_count");
                }
            }
        }

        // Write back with incremented values
        String upsert = "INSERT OR REPLACE INTO category_stats " +
                "(user_id, category, solved_count, attempted_count) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(upsert)) {
            ps.setInt(1, userId);
            ps.setString(2, category.name());
            ps.setInt(3, existingSolved    + (accepted ? 1 : 0));
            ps.setInt(4, existingAttempted + 1);
            ps.executeUpdate();
        }
    }

    private void updateDifficultyStats(int userId, Difficulty difficulty) throws SQLException {
        int existingCount = 0;
        String select = "SELECT solved_count FROM difficulty_stats " +
                "WHERE user_id = ? AND difficulty = ?";
        try (PreparedStatement ps = connection.prepareStatement(select)) {
            ps.setInt(1, userId);
            ps.setString(2, difficulty.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) existingCount = rs.getInt("solved_count");
            }
        }

        String upsert = "INSERT OR REPLACE INTO difficulty_stats " +
                "(user_id, difficulty, solved_count) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(upsert)) {
            ps.setInt(1, userId);
            ps.setString(2, difficulty.name());
            ps.setInt(3, existingCount + 1);
            ps.executeUpdate();
        }
    }

    /**
     * Returns category stats for a user as a map of category → [solved, attempted].
     * @param userId the user's id
     * @return map of category name to int array {solved, attempted}
     * @throws SQLException on database error
     */
    public java.util.Map<String, int[]> getCategoryStats(int userId) throws SQLException {
        String sql = "SELECT category, solved_count, attempted_count FROM category_stats WHERE user_id = ?";
        java.util.Map<String, int[]> result = new java.util.HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("category"),
                               new int[]{rs.getInt("solved_count"), rs.getInt("attempted_count")});
                }
            }
        }
        return result;
    }

    /**
     * Returns difficulty stats for a user.
     * @param userId the user's id
     * @return map of difficulty name to solved count
     * @throws SQLException on database error
     */
    public java.util.Map<String, Integer> getDifficultyStats(int userId) throws SQLException {
        String sql = "SELECT difficulty, solved_count FROM difficulty_stats WHERE user_id = ?";
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.put(rs.getString("difficulty"), rs.getInt("solved_count"));
            }
        }
        return result;
    }

    /**
     * Returns problems not yet solved by the user.
     * @param userId     the user's id
     * @param category   optional category filter (null = all)
     * @param difficulty optional difficulty filter (null = all)
     * @return list of unsolved problems
     * @throws SQLException on database error
     */
    public List<Problem> getUnsolvedProblems(int userId, Category category, Difficulty difficulty) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM problems WHERE id NOT IN " +
                "(SELECT DISTINCT problem_id FROM solutions WHERE user_id = ? AND accepted = 1)");
        if (category != null) sql.append(" AND category = '").append(category.name()).append("'");
        if (difficulty != null) sql.append(" AND difficulty = '").append(difficulty.name()).append("'");
        sql.append(" ORDER BY acceptance_rate DESC");

        List<Problem> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapProblem(rs));
            }
        }
        return result;
    }
}
