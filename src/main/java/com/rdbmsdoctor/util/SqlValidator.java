package com.rdbmsdoctor.util;

import java.util.regex.Pattern;

public class SqlValidator {

    private static final String BLOCKED_ERROR_MESSAGE = "Only read-only SQL queries are allowed in the playground.";

    // Keywords that mutate data or schema
    private static final Pattern FORBIDDEN_KEYWORDS_PATTERN = Pattern.compile(
        "\\b(DROP|TRUNCATE|DELETE|ALTER|CREATE|GRANT|REVOKE|INSERT|UPDATE|EXEC|EXECUTE|SHUTDOWN)\\b",
        Pattern.CASE_INSENSITIVE
    );

    public static ValidationResult validate(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return new ValidationResult(false, "Query cannot be empty.");
        }

        String trimmed = sql.trim();

        // Strip single line & multi-line comments for validation
        String cleanSql = trimmed
            .replaceAll("--.*", "")
            .replaceAll("/\\*.*?\\*/", "")
            .trim();

        if (cleanSql.isEmpty()) {
            return new ValidationResult(false, "Query cannot be empty.");
        }

        // Check if query starts with SELECT or WITH
        String upper = cleanSql.toUpperCase();
        if (!upper.startsWith("SELECT") && !upper.startsWith("WITH") && !upper.startsWith("EXPLAIN")) {
            return new ValidationResult(false, BLOCKED_ERROR_MESSAGE);
        }

        // Check for forbidden keywords anywhere in the query
        if (FORBIDDEN_KEYWORDS_PATTERN.matcher(cleanSql).find()) {
            return new ValidationResult(false, BLOCKED_ERROR_MESSAGE);
        }

        // Check for semicolon-separated multiple statements (prevent stacked queries with dangerous commands)
        String[] statements = cleanSql.split(";");
        int count = 0;
        for (String stmt : statements) {
            if (!stmt.trim().isEmpty()) {
                count++;
            }
        }
        if (count > 1) {
            return new ValidationResult(false, "Multiple SQL statements are not allowed. Please execute one query at a time.");
        }

        return new ValidationResult(true, null);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
