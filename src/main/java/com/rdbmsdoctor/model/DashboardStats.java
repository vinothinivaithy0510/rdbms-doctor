package com.rdbmsdoctor.model;

import java.util.List;
import java.util.Map;

public class DashboardStats {
    private long totalQueriesExecuted;
    private long successfulQueries;
    private long sqlErrorsDetected;
    private long quizQuestionsAttempted;
    private double quizScore;
    private List<Map<String, Object>> recentQueryHistory;
    private List<Map<String, Object>> topicProgress;

    public DashboardStats() {}

    public DashboardStats(long totalQueriesExecuted, long successfulQueries, long sqlErrorsDetected, long quizQuestionsAttempted, double quizScore, List<Map<String, Object>> recentQueryHistory, List<Map<String, Object>> topicProgress) {
        this.totalQueriesExecuted = totalQueriesExecuted;
        this.successfulQueries = successfulQueries;
        this.sqlErrorsDetected = sqlErrorsDetected;
        this.quizQuestionsAttempted = quizQuestionsAttempted;
        this.quizScore = quizScore;
        this.recentQueryHistory = recentQueryHistory;
        this.topicProgress = topicProgress;
    }

    public long getTotalQueriesExecuted() { return totalQueriesExecuted; }
    public void setTotalQueriesExecuted(long totalQueriesExecuted) { this.totalQueriesExecuted = totalQueriesExecuted; }

    public long getSuccessfulQueries() { return successfulQueries; }
    public void setSuccessfulQueries(long successfulQueries) { this.successfulQueries = successfulQueries; }

    public long getSqlErrorsDetected() { return sqlErrorsDetected; }
    public void setSqlErrorsDetected(long sqlErrorsDetected) { this.sqlErrorsDetected = sqlErrorsDetected; }

    public long getQuizQuestionsAttempted() { return quizQuestionsAttempted; }
    public void setQuizQuestionsAttempted(long quizQuestionsAttempted) { this.quizQuestionsAttempted = quizQuestionsAttempted; }

    public double getQuizScore() { return quizScore; }
    public void setQuizScore(double quizScore) { this.quizScore = quizScore; }

    public List<Map<String, Object>> getRecentQueryHistory() { return recentQueryHistory; }
    public void setRecentQueryHistory(List<Map<String, Object>> recentQueryHistory) { this.recentQueryHistory = recentQueryHistory; }

    public List<Map<String, Object>> getTopicProgress() { return topicProgress; }
    public void setTopicProgress(List<Map<String, Object>> topicProgress) { this.topicProgress = topicProgress; }
}
