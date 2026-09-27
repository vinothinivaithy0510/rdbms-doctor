package com.rdbmsdoctor.service;

import com.rdbmsdoctor.model.QueryAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiExplanationService {

    private static final Logger log = LoggerFactory.getLogger(AiExplanationService.class);

    private final QueryDoctorService queryDoctorService;

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    @Autowired
    public AiExplanationService(QueryDoctorService queryDoctorService) {
        this.queryDoctorService = queryDoctorService;
    }

    public QueryAnalysisResult analyzeQueryWithAiFallback(String sql) {
        // Fallback to Java rule engine by default or if AI key is missing
        if (!aiEnabled || apiKey == null || apiKey.trim().isEmpty()) {
            log.info("AI API Key not present. Using native Java Rule Engine for Query Doctor analysis.");
            return queryDoctorService.analyzeQuery(sql);
        }

        try {
            // If AI is configured, rule engine is still performed first to get deterministic analysis
            QueryAnalysisResult ruleResult = queryDoctorService.analyzeQuery(sql);
            // Enrich result if needed
            return ruleResult;
        } catch (Exception e) {
            log.warn("AI service call failed. Falling back to native Query Doctor rule engine: {}", e.getMessage());
            return queryDoctorService.analyzeQuery(sql);
        }
    }
}
