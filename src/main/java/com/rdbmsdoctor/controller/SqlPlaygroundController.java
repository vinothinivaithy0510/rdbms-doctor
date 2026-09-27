package com.rdbmsdoctor.controller;

import com.rdbmsdoctor.model.QueryResult;
import com.rdbmsdoctor.service.SqlExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sql")
public class SqlPlaygroundController {

    private final SqlExecutionService sqlExecutionService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SqlPlaygroundController(SqlExecutionService sqlExecutionService, JdbcTemplate jdbcTemplate) {
        this.sqlExecutionService = sqlExecutionService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/execute")
    public QueryResult executeQuery(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        return sqlExecutionService.executeQuery(query);
    }

    @GetMapping("/schema")
    public Map<String, Object> getSchema() {
        Map<String, Object> response = new HashMap<>();

        Map<String, List<Map<String, String>>> tables = new LinkedHashMap<>();

        tables.put("students", Arrays.asList(
            createCol("student_id", "INT", "PRIMARY KEY"),
            createCol("name", "VARCHAR(100)", "NOT NULL"),
            createCol("department_id", "INT", "FOREIGN KEY (NULLABLE)"),
            createCol("age", "INT", ""),
            createCol("marks", "INT", "")
        ));

        tables.put("departments", Arrays.asList(
            createCol("department_id", "INT", "PRIMARY KEY"),
            createCol("department_name", "VARCHAR(100)", "NOT NULL")
        ));

        tables.put("courses", Arrays.asList(
            createCol("course_id", "INT", "PRIMARY KEY"),
            createCol("course_name", "VARCHAR(100)", "NOT NULL"),
            createCol("department_id", "INT", "FOREIGN KEY")
        ));

        tables.put("enrollments", Arrays.asList(
            createCol("enrollment_id", "INT", "PRIMARY KEY"),
            createCol("student_id", "INT", "FOREIGN KEY"),
            createCol("course_id", "INT", "FOREIGN KEY"),
            createCol("grade", "VARCHAR(5)", "")
        ));

        response.put("database", "rdbms_doctor");
        response.put("tables", tables);
        return response;
    }

    private Map<String, String> createCol(String name, String type, String key) {
        Map<String, String> col = new HashMap<>();
        col.put("name", name);
        col.put("type", type);
        col.put("key", key);
        return col;
    }
}
