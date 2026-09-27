package com.rdbmsdoctor.controller;

import com.rdbmsdoctor.model.QueryAnalysisRequest;
import com.rdbmsdoctor.model.QueryAnalysisResult;
import com.rdbmsdoctor.service.AiExplanationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/query-doctor")
public class QueryDoctorController {

    private final AiExplanationService aiExplanationService;

    @Autowired
    public QueryDoctorController(AiExplanationService aiExplanationService) {
        this.aiExplanationService = aiExplanationService;
    }

    @PostMapping("/analyze")
    public QueryAnalysisResult analyzeQuery(@RequestBody QueryAnalysisRequest request) {
        return aiExplanationService.analyzeQueryWithAiFallback(request.getQuery());
    }
}
