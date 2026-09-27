package com.rdbmsdoctor.model;

import java.util.List;

public class QueryAnalysisResult {
    private String status; // "LOOKS_GOOD", "POSSIBLE_ISSUE", "SQL_ERROR"
    private String problem;
    private String correctedQuery;
    private String explanation;
    private List<String> sqlConcepts;
    private String difficulty; // "Beginner", "Intermediate", "Advanced"

    public QueryAnalysisResult() {}

    public QueryAnalysisResult(String status, String problem, String correctedQuery, String explanation, List<String> sqlConcepts, String difficulty) {
        this.status = status;
        this.problem = problem;
        this.correctedQuery = correctedQuery;
        this.explanation = explanation;
        this.sqlConcepts = sqlConcepts;
        this.difficulty = difficulty;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProblem() { return problem; }
    public void setProblem(String problem) { this.problem = problem; }

    public String getCorrectedQuery() { return correctedQuery; }
    public void setCorrectedQuery(String correctedQuery) { this.correctedQuery = correctedQuery; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getSqlConcepts() { return sqlConcepts; }
    public void setSqlConcepts(List<String> sqlConcepts) { this.sqlConcepts = sqlConcepts; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}
