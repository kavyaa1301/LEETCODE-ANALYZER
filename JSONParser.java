package com.leetcode.analyzer.parser;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.leetcode.analyzer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses LeetCode problem data from a JSON file.
 *
 * <p>Expected JSON format:
 * <pre>
 * {
 *   "problems": [
 *     {
 *       "id": 1,
 *       "title": "Two Sum",
 *       "category": "Array",
 *       "difficulty": "Easy",
 *       "acceptanceRate": 48.2,
 *       "companies": 92,
 *       "tags": ["Array", "Hash Table"]
 *     }
 *   ]
 * }
 * </pre>
 */
public class JSONParser {

    private static final Logger logger = LoggerFactory.getLogger(JSONParser.class);
    private final Gson gson;

    public JSONParser() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Parses problems from the given JSON file path.
     *
     * @param filePath path to the JSON file
     * @return list of parsed Problem objects
     * @throws IOException   if the file cannot be read
     * @throws JsonParseException if JSON is malformed
     */
    public List<Problem> parseProblems(String filePath) throws IOException {
        logger.info("Parsing problems from JSON: {}", filePath);
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return parseFromString(content);
    }

    /**
     * Parses problems from a JSON string.
     *
     * @param jsonContent the raw JSON string
     * @return list of parsed Problem objects
     */
    public List<Problem> parseFromString(String jsonContent) {
        List<Problem> problems = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();
            JsonArray array = root.getAsJsonArray("problems");

            for (JsonElement el : array) {
                try {
                    problems.add(parseProblem(el.getAsJsonObject()));
                } catch (Exception e) {
                    logger.warn("Skipping malformed problem entry: {}", e.getMessage());
                }
            }
            logger.info("Parsed {} problems from JSON", problems.size());
        } catch (JsonParseException e) {
            logger.error("Failed to parse JSON content", e);
            throw e;
        }
        return problems;
    }

    private Problem parseProblem(JsonObject obj) {
        Problem p = new Problem();

        if (!obj.has("id") || !obj.has("title")) {
            throw new IllegalArgumentException("Problem missing required fields: id or title");
        }

        p.setId(obj.get("id").getAsInt());
        p.setTitle(obj.get("title").getAsString().trim());

        if (obj.has("category")) {
            p.setCategory(Category.fromString(obj.get("category").getAsString()));
        } else {
            p.setCategory(Category.UNKNOWN);
        }

        if (obj.has("difficulty")) {
            p.setDifficulty(Difficulty.fromString(obj.get("difficulty").getAsString()));
        } else {
            p.setDifficulty(Difficulty.MEDIUM);
        }

        if (obj.has("acceptanceRate")) {
            double rate = obj.get("acceptanceRate").getAsDouble();
            if (rate < 0 || rate > 100) {
                logger.warn("Problem {} has invalid acceptance rate: {}", p.getId(), rate);
                rate = Math.max(0, Math.min(100, rate));
            }
            p.setAcceptanceRate(rate);
        }

        if (obj.has("companies")) {
            p.setCompaniesCount(obj.get("companies").getAsInt());
        }

        if (obj.has("tags") && obj.get("tags").isJsonArray()) {
            for (JsonElement tag : obj.getAsJsonArray("tags")) {
                p.addTag(tag.getAsString());
            }
        }

        if (obj.has("url")) {
            p.setUrl(obj.get("url").getAsString());
        }

        return p;
    }

    /**
     * Writes a list of problems to a JSON file.
     *
     * @param problems  the problems to write
     * @param filePath  the output path
     * @throws IOException if the file cannot be written
     */
    public void writeProblems(List<Problem> problems, String filePath) throws IOException {
        JsonObject root = new JsonObject();
        JsonArray array = new JsonArray();
        for (Problem p : problems) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", p.getId());
            obj.addProperty("title", p.getTitle());
            obj.addProperty("category", p.getCategory().getDisplayName());
            obj.addProperty("difficulty", p.getDifficulty().getDisplayName());
            obj.addProperty("acceptanceRate", p.getAcceptanceRate());
            obj.addProperty("companies", p.getCompaniesCount());
            JsonArray tags = new JsonArray();
            p.getTags().forEach(tags::add);
            obj.add("tags", tags);
            array.add(obj);
        }
        root.add("problems", array);
        Files.write(Paths.get(filePath), gson.toJson(root).getBytes());
        logger.info("Wrote {} problems to {}", problems.size(), filePath);
    }
}
