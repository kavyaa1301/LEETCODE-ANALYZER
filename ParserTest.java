package com.leetcode.analyzer.parser;

import com.leetcode.analyzer.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JSONParser and CSVParser.
 */
@DisplayName("Parser Tests")
class ParserTest {

    @TempDir
    Path tempDir;

    // ── JSONParser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("JSONParser: parses valid JSON correctly")
    void testJsonParserValid() throws IOException {
        String json = """
            {
              "problems": [
                {"id": 1, "title": "Two Sum", "category": "Array", "difficulty": "Easy",
                 "acceptanceRate": 49.1, "companies": 92, "tags": ["Array", "Hash Table"]},
                {"id": 2, "title": "Add Two Numbers", "category": "Linked List",
                 "difficulty": "Medium", "acceptanceRate": 40.2, "companies": 65, "tags": []}
              ]
            }
            """;

        JSONParser parser = new JSONParser();
        List<Problem> problems = parser.parseFromString(json);

        assertEquals(2, problems.size());

        Problem first = problems.get(0);
        assertEquals(1, first.getId());
        assertEquals("Two Sum", first.getTitle());
        assertEquals(Category.ARRAY, first.getCategory());
        assertEquals(Difficulty.EASY, first.getDifficulty());
        assertEquals(49.1, first.getAcceptanceRate(), 0.001);
        assertTrue(first.getTags().contains("Array"));
        assertTrue(first.getTags().contains("Hash Table"));
    }

    @Test
    @DisplayName("JSONParser: handles empty problems array")
    void testJsonParserEmpty() {
        JSONParser parser = new JSONParser();
        List<Problem> problems = parser.parseFromString("{\"problems\": []}");
        assertNotNull(problems);
        assertTrue(problems.isEmpty());
    }

    @Test
    @DisplayName("JSONParser: skips malformed entries and continues")
    void testJsonParserMalformed() {
        String json = """
            {
              "problems": [
                {"id": 1, "title": "Valid", "category": "Array", "difficulty": "Easy", "acceptanceRate": 50},
                {"title": "Missing ID"},
                {"id": 3, "title": "Also Valid", "category": "String", "difficulty": "Hard", "acceptanceRate": 30}
              ]
            }
            """;
        JSONParser parser = new JSONParser();
        List<Problem> problems = parser.parseFromString(json);
        assertEquals(2, problems.size());
        assertEquals(1, problems.get(0).getId());
        assertEquals(3, problems.get(1).getId());
    }

    @Test
    @DisplayName("JSONParser: unknown category maps to UNKNOWN")
    void testJsonParserUnknownCategory() {
        String json = """
            {"problems": [
              {"id": 10, "title": "Test", "category": "Nonsense Category", "difficulty": "Easy", "acceptanceRate": 50}
            ]}
            """;
        JSONParser parser = new JSONParser();
        List<Problem> problems = parser.parseFromString(json);
        assertEquals(1, problems.size());
        assertEquals(Category.UNKNOWN, problems.get(0).getCategory());
    }

    @Test
    @DisplayName("JSONParser: parses from file")
    void testJsonParserFromFile() throws IOException {
        String json = """
            {"problems": [
              {"id": 100, "title": "File Test", "category": "Graph", "difficulty": "Hard", "acceptanceRate": 25}
            ]}
            """;
        Path file = tempDir.resolve("test.json");
        Files.writeString(file, json);

        JSONParser parser = new JSONParser();
        List<Problem> problems = parser.parseProblems(file.toString());
        assertEquals(1, problems.size());
        assertEquals("File Test", problems.get(0).getTitle());
    }

    @Test
    @DisplayName("JSONParser: round-trip write and read")
    void testJsonParserRoundTrip() throws IOException {
        Problem original = new Problem(42, "Round Trip Test", Category.TREE, Difficulty.MEDIUM, 60.0);
        original.addTag("Tree");
        original.addTag("DFS");

        Path outFile = tempDir.resolve("out.json");
        JSONParser parser = new JSONParser();
        parser.writeProblems(List.of(original), outFile.toString());

        List<Problem> parsed = parser.parseProblems(outFile.toString());
        assertEquals(1, parsed.size());
        assertEquals(42, parsed.get(0).getId());
        assertEquals("Round Trip Test", parsed.get(0).getTitle());
    }

    // ── CSVParser ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CSVParser: parses valid CSV correctly")
    void testCsvParserValid() throws IOException {
        String csv = "id,title,category,difficulty,acceptancerate,companies,tags\n" +
                     "1,Two Sum,Array,Easy,49.1,92,Array|Hash Table\n" +
                     "2,Add Two Numbers,Linked List,Medium,40.2,65,Linked List\n";

        Path file = tempDir.resolve("problems.csv");
        Files.writeString(file, csv);

        CSVParser parser = new CSVParser();
        List<Problem> problems = parser.parseProblems(file.toString());
        assertEquals(2, problems.size());
        assertEquals("Two Sum", problems.get(0).getTitle());
        assertEquals(Category.ARRAY, problems.get(0).getCategory());
        assertEquals(Difficulty.EASY, problems.get(0).getDifficulty());
        assertEquals(49.1, problems.get(0).getAcceptanceRate(), 0.001);
        assertTrue(problems.get(0).getTags().contains("Array"));
        assertTrue(problems.get(0).getTags().contains("Hash Table"));
    }

    @Test
    @DisplayName("CSVParser: skips malformed rows")
    void testCsvParserMalformedRows() throws IOException {
        String csv = "id,title,category,difficulty,acceptancerate,companies,tags\n" +
                     "1,Valid Row,Array,Easy,50,10,Array\n" +
                     "notAnInt,Bad ID Row,Array,Easy,50,10,Array\n" +
                     "3,Another Valid,String,Medium,45,20,String\n";
        Path file = tempDir.resolve("bad.csv");
        Files.writeString(file, csv);

        CSVParser parser = new CSVParser();
        List<Problem> problems = parser.parseProblems(file.toString());
        assertEquals(2, problems.size());
    }

    @Test
    @DisplayName("CSVParser: round-trip write and read")
    void testCsvParserRoundTrip() throws IOException {
        Problem original = new Problem(77, "CSV Trip", Category.GRAPH, Difficulty.HARD, 22.5);
        original.addTag("DFS");
        original.addTag("BFS");

        Path file = tempDir.resolve("round.csv");
        CSVParser parser = new CSVParser();
        parser.writeProblems(List.of(original), file.toString());

        List<Problem> parsed = parser.parseProblems(file.toString());
        assertEquals(1, parsed.size());
        assertEquals(77, parsed.get(0).getId());
        assertEquals("CSV Trip", parsed.get(0).getTitle());
    }

    @Test
    @DisplayName("CSVParser: pipe-separated tags are split correctly")
    void testCsvParserTags() throws IOException {
        String csv = "id,title,category,difficulty,acceptancerate,companies,tags\n" +
                     "5,Tag Test,Array,Easy,50,5,One|Two|Three\n";
        Path file = tempDir.resolve("tags.csv");
        Files.writeString(file, csv);

        CSVParser parser = new CSVParser();
        List<Problem> problems = parser.parseProblems(file.toString());
        assertEquals(3, problems.get(0).getTags().size());
    }
}
