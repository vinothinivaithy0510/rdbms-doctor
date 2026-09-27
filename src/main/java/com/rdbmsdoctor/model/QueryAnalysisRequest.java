package com.rdbmsdoctor.model;

public class QueryAnalysisRequest {
    private String query;

    public QueryAnalysisRequest() {}

    public QueryAnalysisRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
