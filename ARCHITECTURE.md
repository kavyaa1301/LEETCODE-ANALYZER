# Architecture Documentation

## Class Diagram (ASCII)

```
┌──────────────────────────────────────────────────────────────────────┐
│                          com.leetcode.analyzer                        │
│                                                                       │
│  Main ──────────────────────────────────────────────────────────────┐│
│      │                                                               ││
│      ▼                                                               ││
│  CLIInterface                                                        ││
│      │                                                               ││
│      ├──► DatabaseManager (Singleton)                               ││
│      │         │                                                     ││
│      │         ├── addProblem / getProblemById / getAllProblems      ││
│      │         ├── saveUser / getUserByUsername                      ││
│      │         ├── saveSolution / getUserSolutions                   ││
│      │         └── getCategoryStats / getDifficultyStats            ││
│      │                                                               ││
│      ├──► AnalyticsEngine                                           ││
│      │         │                                                     ││
│      │         ├── getWeakAreas()                                   ││
│      │         ├── getDifficultySummary()                           ││
│      │         ├── getProgressOverTime()                            ││
│      │         ├── generateStatistics() ──► Statistics              ││
│      │         └── getCurrentStreak()                               ││
│      │                                                               ││
│      ├──► RecommendationEngine                                      ││
│      │         │                                                     ││
│      │         ├── recommendByWeakArea()                            ││
│      │         ├── recommendByDifficulty()                          ││
│      │         └── generateLearningPath()                           ││
│      │                                                               ││
│      ├──► JSONParser / CSVParser                                    ││
│      │         └── parseProblems() / writeProblems()                ││
│      │                                                               ││
│      └──► ReportGenerator (Interface)                               ││
│                │                                                     ││
│                ├── ConsoleReportGenerator                           ││
│                ├── CSVReportGenerator                               ││
│                └── PDFReportGenerator                               ││
│                                                                      ││
│  Model Layer:                                                        ││
│    Problem ──► Category (enum) + Difficulty (enum)                  ││
│    Solution                                                          ││
│    UserProfile ──► Statistics                                        ││
└──────────────────────────────────────────────────────────────────────┘
```

---

## Design Patterns

### Singleton — `DatabaseManager`
One shared database connection for the lifetime of the application.
Prevents resource leaks and race conditions in single-threaded CLI usage.

```java
DatabaseManager db = DatabaseManager.getInstance();
db.initialize();
```

### Strategy — `ReportGenerator`
The report interface allows the CLI to swap between PDF, CSV, and Console output
without changing the calling code:

```java
ReportGenerator gen = new PDFReportGenerator(); // or CSV, Console
gen.generateReport(user, stats, "output.pdf");
```

### Factory Method (static) — `Category.fromString()`, `Difficulty.fromString()`
Provides safe parsing from strings (JSON/CSV input) to strongly-typed enums,
returning sensible defaults instead of throwing exceptions:

```java
Category cat = Category.fromString("Dynamic Programming"); // → Category.DYNAMIC_PROGRAMMING
```

---

## Database Schema

```
problems            users               solutions
──────────────      ─────────────────   ─────────────────────
id (PK)             id (PK, AUTO)       id (PK, AUTO)
title               username (UNIQUE)   problem_id (FK)
category            email               user_id (FK)
difficulty          created_at          code
acceptance_rate     last_active         time_complexity
companies_count                         space_complexity
tags                                    attempts
url                                     approach
                                        accepted
                                        solved_at
                                        notes
                                        language

category_stats               difficulty_stats
──────────────────           ─────────────────
user_id (PK, FK)             user_id (PK, FK)
category (PK)                difficulty (PK)
solved_count                 solved_count
attempted_count
```

---

## Recommendation Algorithm

### Weak Area Recommendations
- Time: **O(n log n)** — n = number of category stats rows
- Space: **O(n)**

Steps:
1. Fetch `category_stats` for user from DB
2. Filter categories with `solved/attempted < 0.5`
3. For each weak category, fetch unsolved problems sorted by acceptance rate DESC
4. Return top 8 problems (easier problems first to build confidence)

### Difficulty Prediction
- Time: **O(1)**

```
overallSuccessRate < 0.40  → EASY
0.40 ≤ rate < 0.70         → MEDIUM
rate ≥ 0.70                → HARD
```

### Learning Path
- Time: **O(weeks × categories × problems)**

For each week:
1. Escalate difficulty every 2 weeks
2. Rotate category list based on week seed (ensures diversity)
3. Pick 2 unsolved problems per category until week quota (5/week) is met

---

## Performance Considerations

- All DB queries use **prepared statements** to prevent SQL injection and enable query plan caching
- Category and difficulty stats are maintained as **aggregated counters** (updated on every solution insert) rather than computed at query time — this makes analytics O(1) per category instead of O(solutions)
- The SQLite file is kept in the working directory — no server, no port, zero config
- Streams API is used for in-memory aggregations to keep code readable and leverage lazy evaluation

---

## Java Concepts Demonstrated

| Concept | Where |
|---------|-------|
| OOP (encapsulation, polymorphism) | All model classes; `ReportGenerator` strategy |
| Enums with methods | `Category`, `Difficulty` |
| Collections Framework | `HashMap`, `LinkedHashMap`, `ArrayList`, `TreeMap`, `EnumMap` |
| Streams & Lambdas | `AnalyticsEngine`, `RecommendationEngine` |
| JDBC + try-with-resources | `DatabaseManager` |
| Prepared Statements | All DB methods |
| File I/O | `JSONParser`, `CSVParser` — NIO `Files` API |
| Design Patterns | Singleton, Strategy, Factory |
| JUnit 5 | All test classes |
| Generics | Method return types throughout |
| Exception Handling | Every public method has try/catch + logging |
