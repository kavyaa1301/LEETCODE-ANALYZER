-- ============================================================
-- LeetCode Analyzer — SQLite Database Schema
-- ============================================================

-- Problems table: stores all LeetCode problems
CREATE TABLE IF NOT EXISTS problems (
    id               INTEGER PRIMARY KEY,          -- LeetCode problem number
    title            TEXT    NOT NULL,
    category         TEXT    NOT NULL,             -- maps to Category enum
    difficulty       TEXT    NOT NULL,             -- EASY | MEDIUM | HARD
    acceptance_rate  REAL    DEFAULT 0.0,
    companies_count  INTEGER DEFAULT 0,
    tags             TEXT,                         -- comma-separated list
    url              TEXT
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username    TEXT    UNIQUE NOT NULL,
    email       TEXT,
    created_at  TEXT    NOT NULL,
    last_active TEXT
);

-- Solutions table: one row per submission
CREATE TABLE IF NOT EXISTS solutions (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    problem_id       INTEGER NOT NULL,
    user_id          INTEGER NOT NULL,
    code             TEXT,
    time_complexity  TEXT,
    space_complexity TEXT,
    attempts         INTEGER DEFAULT 1,
    approach         TEXT,
    accepted         INTEGER DEFAULT 1,            -- 1 = accepted, 0 = rejected
    solved_at        TEXT,
    notes            TEXT,
    language         TEXT    DEFAULT 'Java',
    FOREIGN KEY (problem_id) REFERENCES problems(id),
    FOREIGN KEY (user_id)    REFERENCES users(id)
);

-- Aggregated stats per user per category
CREATE TABLE IF NOT EXISTS category_stats (
    user_id         INTEGER NOT NULL,
    category        TEXT    NOT NULL,
    solved_count    INTEGER DEFAULT 0,
    attempted_count INTEGER DEFAULT 0,
    PRIMARY KEY (user_id, category)
);

-- Aggregated stats per user per difficulty
CREATE TABLE IF NOT EXISTS difficulty_stats (
    user_id       INTEGER NOT NULL,
    difficulty    TEXT    NOT NULL,
    solved_count  INTEGER DEFAULT 0,
    PRIMARY KEY (user_id, difficulty)
);

-- ── Useful queries ────────────────────────────────────────────────────────────

-- Top 10 weakest categories for user 1
-- SELECT category, CAST(solved_count AS REAL)/attempted_count AS success_rate
-- FROM category_stats WHERE user_id = 1 AND attempted_count > 0
-- ORDER BY success_rate ASC LIMIT 10;

-- Unsolved problems in category 'ARRAY' for user 1
-- SELECT * FROM problems WHERE category = 'ARRAY'
-- AND id NOT IN (SELECT DISTINCT problem_id FROM solutions WHERE user_id = 1 AND accepted = 1);

-- Daily solve counts (last 30 days) for user 1
-- SELECT DATE(solved_at) AS day, COUNT(*) AS solved
-- FROM solutions WHERE user_id = 1 AND accepted = 1
-- AND solved_at >= DATE('now', '-30 days')
-- GROUP BY day ORDER BY day;
