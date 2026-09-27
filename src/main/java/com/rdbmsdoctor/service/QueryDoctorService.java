package com.rdbmsdoctor.service;

import com.rdbmsdoctor.model.QueryAnalysisResult;
import com.rdbmsdoctor.util.SqlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QueryDoctorService {

    private final JdbcTemplate jdbcTemplate;

    // Database metadata definitions for offline rule validation
    private static final Map<String, List<String>> TABLE_COLUMNS = new HashMap<>();
    private static final Map<String, String> COMMON_TABLE_TYPOS = new HashMap<>();
    private static final Map<String, String> COMMON_COLUMN_TYPOS = new HashMap<>();

    static {
        TABLE_COLUMNS.put("students", Arrays.asList("student_id", "name", "department_id", "age", "marks"));
        TABLE_COLUMNS.put("departments", Arrays.asList("department_id", "department_name"));
        TABLE_COLUMNS.put("courses", Arrays.asList("course_id", "course_name", "department_id"));
        TABLE_COLUMNS.put("enrollments", Arrays.asList("enrollment_id", "student_id", "course_id", "grade"));

        // Table typos
        COMMON_TABLE_TYPOS.put("student", "students");
        COMMON_TABLE_TYPOS.put("dept", "departments");
        COMMON_TABLE_TYPOS.put("department", "departments");
        COMMON_TABLE_TYPOS.put("course", "courses");
        COMMON_TABLE_TYPOS.put("enrollment", "enrollments");
        COMMON_TABLE_TYPOS.put("std", "students");
        COMMON_TABLE_TYPOS.put("stud", "students");

        // Column typos
        COMMON_COLUMN_TYPOS.put("nam", "name");
        COMMON_COLUMN_TYPOS.put("sname", "name");
        COMMON_COLUMN_TYPOS.put("st_name", "name");
        COMMON_COLUMN_TYPOS.put("student_name", "name");
        COMMON_COLUMN_TYPOS.put("dep", "department_id");
        COMMON_COLUMN_TYPOS.put("dep_id", "department_id");
        COMMON_COLUMN_TYPOS.put("dept_id", "department_id");
        COMMON_COLUMN_TYPOS.put("department", "department_id");
        COMMON_COLUMN_TYPOS.put("dept_name", "department_name");
        COMMON_COLUMN_TYPOS.put("dname", "department_name");
        COMMON_COLUMN_TYPOS.put("cname", "course_name");
        COMMON_COLUMN_TYPOS.put("course_title", "course_name");
        COMMON_COLUMN_TYPOS.put("marks_got", "marks");
        COMMON_COLUMN_TYPOS.put("mark", "marks");
        COMMON_COLUMN_TYPOS.put("score", "marks");
        COMMON_COLUMN_TYPOS.put("st_id", "student_id");
        COMMON_COLUMN_TYPOS.put("stud_id", "student_id");
        COMMON_COLUMN_TYPOS.put("crs_id", "course_id");
    }

    @Autowired
    public QueryDoctorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public QueryAnalysisResult analyzeQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return new QueryAnalysisResult(
                "SQL_ERROR",
                "The query is empty.",
                "SELECT * FROM students;",
                "Please enter a valid SQL query to analyze.",
                Arrays.asList("SELECT"),
                "Beginner"
            );
        }

        String rawSql = sql.trim();
        String upperSql = rawSql.toUpperCase();

        // Rule 1: Check for Dangerous/Write SQL
        SqlValidator.ValidationResult securityCheck = SqlValidator.validate(rawSql);
        if (!securityCheck.isValid() && securityCheck.getErrorMessage().contains("read-only")) {
            return new QueryAnalysisResult(
                "SQL_ERROR",
                "Dangerous or mutating SQL statement detected.",
                "SELECT * FROM students;",
                "The playground and doctor only permit safe read-only SQL queries (SELECT or WITH). Statements like DROP, DELETE, UPDATE, or ALTER are restricted.",
                Arrays.asList("SECURITY", "SELECT"),
                "Beginner"
            );
        }

        // Rule 2: Check for SELECT without FROM clause
        if (upperSql.startsWith("SELECT") && !upperSql.contains(" FROM ") && !upperSql.matches("(?i).*SELECT\\s+\\d+.*")) {
            String corrected = rawSql + " FROM students;";
            return new QueryAnalysisResult(
                "SQL_ERROR",
                "SELECT statement is missing a FROM clause.",
                corrected,
                "In SQL, a SELECT statement needs a FROM clause to specify which table to retrieve data from.",
                Arrays.asList("SELECT", "FROM"),
                "Beginner"
            );
        }

        // Rule 3: Check NULL comparisons (= NULL or != NULL instead of IS NULL / IS NOT NULL)
        if (Pattern.compile("(?i)=\\s*NULL\\b").matcher(rawSql).find()) {
            String corrected = rawSql.replaceAll("(?i)=\\s*NULL\\b", "IS NULL");
            return new QueryAnalysisResult(
                "POSSIBLE_ISSUE",
                "Comparison using '= NULL' will always return UNKNOWN (false) in SQL.",
                corrected,
                "In SQL, NULL represents an missing or unknown value. You cannot compare values to NULL using '='. Always use 'IS NULL' or 'IS NOT NULL'.",
                Arrays.asList("NULL", "IS NULL", "WHERE"),
                "Beginner"
            );
        }
        if (Pattern.compile("(?i)(!=|<>)\\s*NULL\\b").matcher(rawSql).find()) {
            String corrected = rawSql.replaceAll("(?i)(!=|<>)\\s*NULL\\b", "IS NOT NULL");
            return new QueryAnalysisResult(
                "POSSIBLE_ISSUE",
                "Comparison using '!= NULL' or '<> NULL' does not work in SQL.",
                corrected,
                "Comparing anything with NULL using inequality operators returns UNKNOWN. Use 'IS NOT NULL' to check for non-null values.",
                Arrays.asList("NULL", "IS NOT NULL", "WHERE"),
                "Beginner"
            );
        }

        // Rule 4: Check JOIN without ON clause
        if (Pattern.compile("(?i)\\bJOIN\\b").matcher(rawSql).find() && !Pattern.compile("(?i)\\bON\\b").matcher(rawSql).find() && !Pattern.compile("(?i)\\bUSING\\b").matcher(rawSql).find()) {
            String corrected = rawSql;
            if (rawSql.toLowerCase().contains("departments")) {
                corrected = rawSql.replaceAll("(?i)departments(\\s+\\w+)?", "$0 ON s.department_id = d.department_id");
            } else {
                corrected = rawSql + " ON t1.id = t2.id";
            }
            return new QueryAnalysisResult(
                "SQL_ERROR",
                "The JOIN clause does not contain an ON condition.",
                corrected,
                "A JOIN specifies how rows from two tables should be matched. Without an ON condition, SQL cannot relate the tables.",
                Arrays.asList("JOIN", "ON", "RELATIONAL_DATA"),
                "Intermediate"
            );
        }

        // Rule 5: Check Aggregate function in WHERE clause (e.g. WHERE AVG(marks) > 80)
        Matcher whereAggMatcher = Pattern.compile("(?i)\\bWHERE\\s+.*\\b(AVG|COUNT|SUM|MAX|MIN)\\s*\\(").matcher(rawSql);
        if (whereAggMatcher.find()) {
            String corrected = rawSql.replaceAll("(?i)\\bWHERE\\b", "HAVING");
            if (!upperSql.contains("GROUP BY")) {
                corrected = rawSql.replaceAll("(?i)\\bWHERE\\s+(.*)", "GROUP BY department_id HAVING $1");
            }
            return new QueryAnalysisResult(
                "SQL_ERROR",
                "Aggregate functions (AVG, COUNT, SUM, MAX, MIN) cannot be placed inside a WHERE clause.",
                corrected,
                "WHERE filters individual rows before aggregation. To filter grouped aggregate results, use the HAVING clause with GROUP BY.",
                Arrays.asList("WHERE", "HAVING", "GROUP BY", "AGGREGATION"),
                "Intermediate"
            );
        }

        // Rule 6: Check HAVING used without GROUP BY or used for non-aggregated filter
        if (upperSql.contains(" HAVING ") && !upperSql.contains("GROUP BY")) {
            return new QueryAnalysisResult(
                "POSSIBLE_ISSUE",
                "HAVING clause is used without a GROUP BY clause.",
                rawSql.replaceAll("(?i)\\bHAVING\\b", "WHERE"),
                "The HAVING clause is designed to filter groups created by GROUP BY. If you are filtering individual rows without grouping, use WHERE instead.",
                Arrays.asList("HAVING", "WHERE", "GROUP BY"),
                "Intermediate"
            );
        }

        // Rule 7: Check for ambiguous JOIN condition (e.g., ON department_id = department_id)
        Matcher ambiguousJoinMatcher = Pattern.compile("(?i)\\bON\\s+([a-zA-Z0-9_]+)\\s*=\\s*\\1\\b").matcher(rawSql);
        if (ambiguousJoinMatcher.find()) {
            String col = ambiguousJoinMatcher.group(1);
            String corrected = rawSql.replaceAll("(?i)\\bON\\s+" + col + "\\s*=\\s*" + col + "\\b", "ON s." + col + " = d." + col);
            return new QueryAnalysisResult(
                "SQL_ERROR",
                "Ambiguous JOIN condition: '" + col + " = " + col + "'. Table aliases are required to distinguish columns from different tables.",
                corrected,
                "When joining tables that share column names, qualify each column with its table alias (e.g., s.department_id = d.department_id) to avoid ambiguity.",
                Arrays.asList("JOIN", "ON", "TABLE_ALIASES"),
                "Intermediate"
            );
        }

        // Rule 8: Check for misspelled table names
        for (Map.Entry<String, String> entry : COMMON_TABLE_TYPOS.entrySet()) {
            String regex = "(?i)\\bFROM\\s+" + entry.getKey() + "\\b|\\bJOIN\\s+" + entry.getKey() + "\\b";
            if (Pattern.compile(regex).matcher(rawSql).find()) {
                String corrected = rawSql.replaceAll("(?i)\\b" + entry.getKey() + "\\b", entry.getValue());
                return new QueryAnalysisResult(
                    "SQL_ERROR",
                    "Table '" + entry.getKey() + "' does not exist in the database.",
                    corrected,
                    "Did you mean table '" + entry.getValue() + "'? The existing tables are: students, departments, courses, enrollments.",
                    Arrays.asList("FROM", "TABLE_NAME", "SCHEMA"),
                    "Beginner"
                );
            }
        }

        // Rule 9: Check for misspelled column names
        for (Map.Entry<String, String> entry : COMMON_COLUMN_TYPOS.entrySet()) {
            String regex = "(?i)\\b" + entry.getKey() + "\\b";
            if (Pattern.compile(regex).matcher(rawSql).find()) {
                // Ensure it's not part of another valid keyword
                String corrected = rawSql.replaceAll("(?i)\\b" + entry.getKey() + "\\b", entry.getValue());
                return new QueryAnalysisResult(
                    "SQL_ERROR",
                    "Column '" + entry.getKey() + "' does not exist in the referenced table.",
                    corrected,
                    "Did you mean column '" + entry.getValue() + "'? Verify table column names in the schema viewer.",
                    Arrays.asList("SELECT", "COLUMN_NAME", "SCHEMA"),
                    "Beginner"
                );
            }
        }

        // Rule 10: Run database validation dry-run via EXPLAIN or query execution
        try {
            // Attempt to execute query on DB
            jdbcTemplate.queryForList(rawSql);

            // Check for potential logical recommendations
            if (upperSql.contains("JOIN") && upperSql.contains("WHERE") && upperSql.contains("NULL")) {
                return new QueryAnalysisResult(
                    "POSSIBLE_ISSUE",
                    "Using WHERE on a LEFT JOIN column might accidentally turn it into an INNER JOIN.",
                    rawSql,
                    "When performing a LEFT JOIN to keep unmatched rows, placing a WHERE filter on the right table's non-null column removes unmatched rows. Consider filtering in the ON clause or using IS NULL.",
                    Arrays.asList("LEFT JOIN", "INNER JOIN", "NULL"),
                    "Advanced"
                );
            }

            // Check if query returns clean result
            List<String> concepts = detectConcepts(rawSql);
            return new QueryAnalysisResult(
                "LOOKS_GOOD",
                "No errors detected! Your SQL query is syntactically correct and executed successfully.",
                rawSql,
                "Great job! The query adheres to standard SQL syntax and retrieves data efficiently.",
                concepts,
                determineDifficulty(concepts)
            );

        } catch (Exception dbEx) {
            String dbMsg = dbEx.getMessage() != null ? dbEx.getMessage() : "";
            
            // Analyze database exception message
            if (dbMsg.contains("Unknown column") || dbMsg.contains("Column not found") || dbMsg.contains("not found")) {
                String extractedCol = extractSymbol(dbMsg, "Unknown column '", "'");
                if (extractedCol.isEmpty()) extractedCol = extractSymbol(dbMsg, "Column \"", "\"");
                
                return new QueryAnalysisResult(
                    "SQL_ERROR",
                    "Column error: " + (extractedCol.isEmpty() ? "Unknown column in query" : "Column '" + extractedCol + "' does not exist."),
                    suggestColumnFix(rawSql, extractedCol),
                    "Check the column spelling against the database schema. Available student columns are: student_id, name, department_id, age, marks.",
                    Arrays.asList("SELECT", "COLUMN_NAME"),
                    "Beginner"
                );
            } else if (dbMsg.contains("Table") && dbMsg.contains("doesn't exist")) {
                String extractedTable = extractSymbol(dbMsg, "Table '", "'");
                return new QueryAnalysisResult(
                    "SQL_ERROR",
                    "Table error: Table '" + extractedTable + "' does not exist.",
                    suggestTableFix(rawSql, extractedTable),
                    "Available tables in rdbms_doctor database: students, departments, courses, enrollments.",
                    Arrays.asList("FROM", "TABLE_NAME"),
                    "Beginner"
                );
            } else if (dbMsg.contains("syntax") || dbMsg.contains("Syntax")) {
                return new QueryAnalysisResult(
                    "SQL_ERROR",
                    "Syntax Error near: " + truncate(dbMsg, 120),
                    rawSql,
                    "Check for missing commas, unmatched quotes, or misplaced SQL keywords.",
                    Arrays.asList("SYNTAX", "SQL_KEYWORDS"),
                    "Beginner"
                );
            }

            return new QueryAnalysisResult(
                "SQL_ERROR",
                "Execution Error: " + truncate(dbMsg, 150),
                rawSql,
                "The database engine could not execute this query. Check keyword ordering: SELECT -> FROM -> JOIN -> WHERE -> GROUP BY -> HAVING -> ORDER BY.",
                Arrays.asList("SQL_SYNTAX"),
                "Intermediate"
            );
        }
    }

    private List<String> detectConcepts(String sql) {
        List<String> concepts = new ArrayList<>();
        String u = sql.toUpperCase();
        if (u.contains("SELECT")) concepts.add("SELECT");
        if (u.contains("WHERE")) concepts.add("WHERE");
        if (u.contains("LEFT JOIN") || u.contains("RIGHT JOIN")) concepts.add("OUTER JOIN");
        else if (u.contains("JOIN")) concepts.add("INNER JOIN");
        if (u.contains("GROUP BY")) concepts.add("GROUP BY");
        if (u.contains("HAVING")) concepts.add("HAVING");
        if (u.contains("ORDER BY")) concepts.add("ORDER BY");
        if (u.contains("COUNT") || u.contains("AVG") || u.contains("SUM") || u.contains("MAX") || u.contains("MIN")) concepts.add("AGGREGATION");
        if (u.contains("IS NULL") || u.contains("IS NOT NULL")) concepts.add("NULL HANDLING");
        if (u.contains("WITH")) concepts.add("CTE");
        if (concepts.isEmpty()) concepts.add("BASIC SQL");
        return concepts;
    }

    private String determineDifficulty(List<String> concepts) {
        if (concepts.contains("CTE") || concepts.contains("HAVING") || concepts.contains("OUTER JOIN")) {
            return "Advanced";
        } else if (concepts.contains("INNER JOIN") || concepts.contains("GROUP BY") || concepts.contains("AGGREGATION")) {
            return "Intermediate";
        }
        return "Beginner";
    }

    private String extractSymbol(String text, String prefix, String suffix) {
        int start = text.indexOf(prefix);
        if (start != -1) {
            start += prefix.length();
            int end = text.indexOf(suffix, start);
            if (end != -1) {
                return text.substring(start, end);
            }
        }
        return "";
    }

    private String suggestColumnFix(String sql, String badCol) {
        if (badCol.isEmpty()) return sql;
        String bestMatch = "name";
        if (badCol.toLowerCase().contains("dep")) bestMatch = "department_id";
        else if (badCol.toLowerCase().contains("mark")) bestMatch = "marks";
        else if (badCol.toLowerCase().contains("id")) bestMatch = "student_id";
        return sql.replaceAll("(?i)\\b" + Pattern.quote(badCol) + "\\b", bestMatch);
    }

    private String suggestTableFix(String sql, String badTable) {
        if (badTable.isEmpty()) return sql;
        String nameOnly = badTable.contains(".") ? badTable.substring(badTable.indexOf(".") + 1) : badTable;
        return sql.replaceAll("(?i)\\b" + Pattern.quote(nameOnly) + "\\b", "students");
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
