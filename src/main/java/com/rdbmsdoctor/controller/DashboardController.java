package com.rdbmsdoctor.controller;

import com.rdbmsdoctor.model.DashboardStats;
import com.rdbmsdoctor.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final StatsService statsService;

    @Autowired
    public DashboardController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public DashboardStats getDashboardStats() {
        return statsService.getDashboardStats();
    }
}
