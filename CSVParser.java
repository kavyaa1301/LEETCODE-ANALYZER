package com.leetcode.analyzer.parser;

import com.leetcode.analyzer.model.*;
import org.apache.commons.csv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parses LeetCode problem data from a CSV file using Apache Commons CSV.
 *
 * <p>Expected CSV headers (case-insensitive):
 * {@code id, title, category, difficulty, acceptancerate, companies, tags}
 *
 * <p>Tags should be pipe-separated within the column: {@code Array|Hash Table}
 */
public class CSVParser {

    private static final Logger logger = LoggerFactory.getLogger(CSVParser.class);

    /**
     * Parses problems from a CSV file.
     *
     * @param filePath path to the CSV file
     * @return list of parsed Problems
     * @throws IOException if the file cannot be read or is malformed
     */
    public List<Problem> parseProblems(String filePath) throws IOException {
        logger.info("Parsing problems from CSV: {}", filePath);
        List<Problem> problems = new ArrayList<>();
        int skipped = 0;

        try (Reader reader = new FileReader(filePath, StandardCharsets.UTF_8);
             org.apache.commons.csv.CSVParser parser = org.apache.commons.csv.CSVParser.parse(
                     reader,
                     CSVFormat.DEFAULT
                         .withFirstRecordAsHeader()
                         .withIgnoreHeaderCase()
                         .withTrim()
                         .withIgnoreEmptyLines())) {

            for (CSVRecord record : parser) {
                try {
                    problems.add(parseRecord(record));
                } catch (Exception e) {
                    logger.warn("Skipping record at line {}: {}", record.getRecordNumber(), e.getMessage());
                    skipped++;
                }
            }
        }

        logger.info("Parsed {} problems from CSV ({} skipped)", problems.size(), skipped);
        return problems;
    }

    private Problem parseProblem(CSVRecord record) {
        return parseRecord(record);
    }

    private Problem parseRecord(CSVRecord record) {
        Problem p = new Problem();

        String idStr = getField(record, "id");
        if (idStr == null || idStr.isEmpty()) throw new IllegalArgumentException("Missing id");
        p.setId(Integer.parseInt(idStr.trim()));

        String title = getField(record, "title");
        if (title == null || title.isEmpty()) throw new IllegalArgumentException("Missing title");
        p.setTitle(title.trim());

        String category = getField(record, "category");
        p.setCategory(category != null ? Category.fromString(category) : Category.UNKNOWN);

        String difficulty = getField(record, "difficulty");
        p.setDifficulty(difficulty != null ? Difficulty.fromString(difficulty) : Difficulty.MEDIUM);

        String rateStr = getField(record, "acceptancerate");
        if (rateStr == null) rateStr = getField(record, "acceptance_rate");
        if (rateStr != null && !rateStr.isEmpty()) {
            try {
                p.setAcceptanceRate(Double.parseDouble(rateStr.replace("%", "").trim()));
            } catch (NumberFormatException ignored) {}
        }

        String companiesStr = getField(record, "companies");
        if (companiesStr != null && !companiesStr.isEmpty()) {
            try { p.setCompaniesCount(Integer.parseInt(companiesStr.trim())); }
            catch (NumberFormatException ignored) {}
        }

        String tags = getField(record, "tags");
        if (tags != null && !tags.isEmpty()) {
            Arrays.stream(tags.split("[|,]"))
                  .map(String::trim)
                  .filter(t -> !t.isEmpty())
                  .forEach(p::addTag);
        }

        return p;
    }

    private String getField(CSVRecord record, String fieldName) {
        try {
            return record.get(fieldName);
        } catch (IllegalArgumentException e) {
            return null; // header not present
        }
    }

    /**
     * Exports a list of problems to a CSV file.
     *
     * @param problems the problems to export
     * @param filePath the output path
     * @throws IOException on write error
     */
    public void writeProblems(List<Problem> problems, String filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("id", "title", "category", "difficulty",
                                 "acceptancerate", "companies", "tags"))) {
            for (Problem p : problems) {
                printer.printRecord(
                        p.getId(),
                        p.getTitle(),
                        p.getCategory().getDisplayName(),
                        p.getDifficulty().getDisplayName(),
                        String.format("%.1f", p.getAcceptanceRate()),
                        p.getCompaniesCount(),
                        String.join("|", p.getTags())
                );
            }
        }
        logger.info("Exported {} problems to {}", problems.size(), filePath);
    }
}
