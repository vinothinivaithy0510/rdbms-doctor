package com.rdbmsdoctor.service;

import com.rdbmsdoctor.model.DashboardStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatsService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public StatsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardStats getDashboardStats() {
        long totalQueries = getCount("SELECT COUNT(*) FROM query_logs");
        long successQueries = getCount("SELECT COUNT(*) FROM query_logs WHERE is_success = true");
        long sqlErrors = getCount("SELECT COUNT(*) FROM query_logs WHERE is_success = false");

        long quizAttempted = getCount("SELECT COUNT(*) FROM quiz_attempts");
        long quizCorrect = getCount("SELECT COUNT(*) FROM quiz_attempts WHERE is_correct = true");

        double quizScorePct = quizAttempted > 0 ? Math.round(((double) quizCorrect / quizAttempted) * 100.0) : 0.0;

        List<Map<String, Object>> recentHistory = getRecentHistory();
        List<Map<String, Object>> topicProgress = calculateTopicProgress();

        return new DashboardStats(
            totalQueries,
            successQueries,
            sqlErrors,
            quizAttempted,
            quizScorePct,
            recentHistory,
            topicProgress
        );
    }

    private long getCount(String sql) {
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private List<Map<String, Object>> getRecentHistory() {
        try {
            return jdbcTemplate.queryForList(
                "SELECT log_id, query_text, is_success, error_message, execution_time_ms, created_at FROM query_logs ORDER BY log_id DESC LIMIT 10"
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> calculateTopicProgress() {
        List<Map<String, Object>> list = new ArrayList<>();

        list.add(createTopicItem("Basic SELECT & Filters", "SELECT, WHERE, ORDER BY, LIMIT", 85, "Mastered"));
        list.add(createTopicItem("Relational JOINs", "INNER JOIN, LEFT JOIN, RIGHT JOIN", 70, "In Progress"));
        list.add(createTopicItem("Aggregations & Grouping", "COUNT, AVG, SUM, GROUP BY, HAVING", 60, "In Progress"));
        list.add(createTopicItem("NULL Value Mechanics", "IS NULL, IS NOT NULL, 3-Valued Logic", 75, "Mastered"));
        list.add(createTopicItem("Subqueries & CTEs", "Nested SELECT, Correlated Subquery, WITH", 45, "Learning"));

        return list;
    }

    private Map<String, Object> createTopicItem(String name, String details, int percentage, String status) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("details", details);
        map.put("percentage", percentage);
        map.put("status", status);
        return map;
    }
}
