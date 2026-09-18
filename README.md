# 🚀 LeetCode Problem Analyzer

A comprehensive Java CLI application that helps competitive programmers track their LeetCode progress, identify weak areas, and receive personalized problem recommendations.

---

## 📋 Problem Statement

Competitive programmers often struggle to identify which areas need improvement, track their progress over time, or decide what to study next. This tool solves those problems by:
- Tracking every solution with full metadata
- Analyzing performance by category and difficulty
- Surfacing weak areas with data-driven recommendations
- Generating downloadable progress reports (PDF & CSV)

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| **Solution Tracking** | Log solutions with code, complexity, approach, and attempts |
| **Analytics Dashboard** | View success rates per category and difficulty |
| **Weak Area Detection** | Automatically surface your lowest-performing topics |
| **Smart Recommendations** | Get personalized problem suggestions |
| **Learning Path** | Generate multi-week study plans |
| **PDF Reports** | Export professional progress reports |
| **CSV Export** | Download solution history as spreadsheet |
| **Problem Search** | Search/filter the problem database |
| **Auto Data Import** | Seed the DB from JSON or CSV on first run |

---

## 🛠 Technical Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 11 |
| Build Tool | Maven |
| Database | SQLite (via sqlite-jdbc) |
| JSON | Google Gson |
| CSV | Apache Commons CSV |
| PDF | iText 5 |
| Logging | SLF4J + slf4j-simple |
| Testing | JUnit 5 |

---

## 📁 Project Structure

```
leetcode-analyzer/
├── data/
│   ├── problems.json        # 60 sample LeetCode problems
│   └── problems.csv         # Same data in CSV format
├── src/
│   ├── main/java/com/leetcode/analyzer/
│   │   ├── Main.java
│   │   ├── analytics/
│   │   │   └── AnalyticsEngine.java
│   │   ├── cli/
│   │   │   └── CLIInterface.java
│   │   ├── database/
│   │   │   └── DatabaseManager.java
│   │   ├── model/
│   │   │   ├── Category.java
│   │   │   ├── Difficulty.java
│   │   │   ├── Problem.java
│   │   │   ├── Solution.java
│   │   │   ├── Statistics.java
│   │   │   └── UserProfile.java
│   │   ├── parser/
│   │   │   ├── JSONParser.java
│   │   │   └── CSVParser.java
│   │   ├── recommendation/
│   │   │   └── RecommendationEngine.java
│   │   ├── report/
│   │   │   ├── ReportGenerator.java       (interface)
│   │   │   ├── ConsoleReportGenerator.java
│   │   │   ├── CSVReportGenerator.java
│   │   │   └── PDFReportGenerator.java
│   │   └── util/
│   │       └── ConsoleFormatter.java
│   └── test/java/com/leetcode/analyzer/
│       ├── analytics/AnalyticsEngineTest.java
│       ├── database/DatabaseManagerTest.java
│       ├── model/ProblemTest.java
│       ├── model/ModelTest.java
│       └── parser/ParserTest.java
├── db_schema.sql
├── pom.xml
└── README.md
```

---

## ⚡ Installation & Setup

### Prerequisites
- Java 11 or higher
- Maven 3.6+

### Build

```bash
# Clone and build
git clone <your-repo-url>
cd leetcode-analyzer
mvn clean package -DskipTests

# Run
java -jar target/leetcode-analyzer.jar
```

The application will automatically import the 60 sample problems from `data/problems.json` on first run.

---

## 🎮 Usage Guide

### Main Menu

```
╔════════════════════════════════════════════════════════════╗
║               LeetCode Problem Analyzer v1.0               ║
╚════════════════════════════════════════════════════════════╝

Enter your username: alice

──────────────────────────────────────────────────────────────
  Main Menu  [alice]
──────────────────────────────────────────────────────────────
  1. Add Solution
  2. View My Statistics
  3. Get Recommendations
  4. Search Problems
  5. View Analytics
  6. Generate Report
  7. Import Problem Data
  8. Exit
```

### Adding a Solution

```
▶ Add Solution
  Problem ID: 1
  Found: [1] Two Sum | Array | Easy | 49.1%

  Approach/Notes: HashMap for O(1) lookups
  Time Complexity (e.g. O(n)): O(n)
  Space Complexity (e.g. O(1)): O(n)
  Attempts taken: 1
  Was it accepted? (y/n): y
  Code (optional, press Enter to skip): 
✔ Solution saved successfully!
```

### Viewing Statistics

```
▶ Overall Statistics
  Total Solved              25
  Total Attempted           30
  Success Rate              83.3%
  Avg Attempts per Problem  1.20
  Strongest Category        Array
  Weakest Category          Dynamic Programming

▶ Performance by Category
  Array                     [████████████████████] 100.0%
  String                    [████████████████░░░░]  82.0%
  Dynamic Programming       [████████░░░░░░░░░░░░]  40.0%
```

### Importing Data

```
▶ Import Problem Data
  1. Import from JSON
  2. Import from CSV
  Choose: 1
  File path: data/problems.json
✔ Imported 60 / 60 problems.
```

---

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=ProblemTest

# Run with coverage report (requires jacoco plugin)
mvn verify
```

Expected output:
```
[INFO] Tests run: 35+, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 🏗 Architecture Overview

### Design Patterns Used

| Pattern | Class | Purpose |
|---------|-------|---------|
| **Singleton** | `DatabaseManager` | Single shared DB connection |
| **Strategy** | `ReportGenerator` | Interchangeable PDF/CSV/Console reports |
| **Factory** (implicit) | `Category.fromString()` | Safe enum creation from strings |

### Data Flow

```
User Input → CLIInterface → (DatabaseManager | AnalyticsEngine | RecommendationEngine)
                                    ↓                    ↓
                              SQLite DB           Statistics/Recommendations
                                    ↓
                          ReportGenerator → PDF / CSV / Console
```

### Recommendation Algorithm

1. **Weak Area** — Categories with success rate < 50% are flagged; problems sorted by acceptance rate (higher = more approachable)
2. **Difficulty** — Predicted based on overall success rate: <40% → Easy, 40–70% → Medium, ≥70% → Hard
3. **Learning Path** — 5 problems/week, difficulty escalates every 2 weeks, categories rotate for diversity

---

## 🔮 Future Enhancements

- [ ] REST API mode (Spring Boot)
- [ ] GUI frontend (JavaFX)
- [ ] LeetCode API integration (auto-import solved problems)
- [ ] Spaced-repetition scheduling
- [ ] Team/leaderboard support
- [ ] Docker container support

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| `Database not initialized` | Ensure `initialize()` is called before use |
| `ClassNotFoundException: org.sqlite.JDBC` | Run `mvn clean package` to bundle deps |
| PDF report fails | Ensure write permission in current directory |
| Import shows 0 problems | Check file path and JSON format (needs `"problems"` root key) |

---

## 📜 License

MIT License — see `LICENSE` for details.
