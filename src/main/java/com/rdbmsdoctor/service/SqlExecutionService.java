package com.rdbmsdoctor.service;

import com.rdbmsdoctor.model.QueryResult;
import com.rdbmsdoctor.util.SqlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SqlExecutionService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SqlExecutionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public QueryResult executeQuery(String sql) {
        // Validate read-only security
        SqlValidator.ValidationResult validation = SqlValidator.validate(sql);
        if (!validation.isValid()) {
            logQueryExecution(sql, false, validation.getErrorMessage(), 0);
            return new QueryResult(false, sql, Collections.emptyList(), Collections.emptyList(), 0, 0, validation.getErrorMessage());
        }

        long startTime = System.currentTimeMillis();

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            long duration = System.currentTimeMillis() - startTime;

            List<String> columns = new ArrayList<>();
            if (!rows.isEmpty()) {
                columns.addAll(rows.get(0).keySet());
            }

            logQueryExecution(sql, true, null, duration);

            return new QueryResult(true, sql, columns, rows, rows.size(), duration, null);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String cleanError = sanitizeErrorMessage(e.getMessage());
            logQueryExecution(sql, false, cleanError, duration);

            return new QueryResult(false, sql, Collections.emptyList(), Collections.emptyList(), 0, duration, cleanError);
        }
    }

    public List<Map<String, Object>> getRecentHistory(int limit) {
        try {
            return jdbcTemplate.queryForList(
                "SELECT log_id, query_text, is_success, error_message, execution_time_ms, created_at FROM query_logs ORDER BY log_id DESC LIMIT ?",
                limit
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private void logQueryExecution(String sql, boolean success, String errorMessage, long durationMs) {
        try {
            jdbcTemplate.update(
                "INSERT INTO query_logs (query_text, is_success, error_message, execution_time_ms) VALUES (?, ?, ?, ?)",
                sql, success, errorMessage, durationMs
            );
        } catch (Exception e) {
            // Silently swallow log write failures if schema is being initialized
        }
    }

    private String sanitizeErrorMessage(String rawMessage) {
        if (rawMessage == null) return "An unknown database error occurred.";
        // Hide internal connection strings, credentials or Java package stack traces
        String clean = rawMessage.replaceAll("(?i)jdbc:[^\\s]+", "[REDACTED DB URL]")
                                  .replaceAll("(?i)user '[^']+'", "user '[REDACTED]'");
        
        // Extract meaningful SQL error message
        if (clean.contains("; nested exception is")) {
            clean = clean.substring(0, clean.indexOf("; nested exception is"));
        }
        return clean;
    }
}
