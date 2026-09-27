package com.rdbmsdoctor.service;

import com.rdbmsdoctor.model.QuizQuestion;
import com.rdbmsdoctor.model.QuizResult;
import com.rdbmsdoctor.model.QuizSubmission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuizService {

    private final JdbcTemplate jdbcTemplate;
    private final List<QuizQuestion> questions = new ArrayList<>();

    @Autowired
    public QuizService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initQuestions();
    }

    private void initQuestions() {
        questions.add(new QuizQuestion(
            1,
            "JOIN Operations",
            "Which SQL JOIN type returns all records from the left table and matched records from the right table, filling non-matching right records with NULL?",
            Arrays.asList("INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL OUTER JOIN"),
            1,
            "LEFT JOIN returns all rows from the left table regardless of whether there is a match in the right table. Missing values from the right table will appear as NULL.",
            "Beginner"
        ));

        questions.add(new QuizQuestion(
            2,
            "NULL Handling",
            "What will be the result of evaluating the predicate 'WHERE department_id = NULL' in standard SQL?",
            Arrays.asList("Returns rows with NULL department_id", "Throws a syntax error", "Returns no rows (UNKNOWN/false)", "Deletes NULL records"),
            2,
            "In SQL, NULL represents an unknown value. Comparisons using '=' with NULL evaluate to UNKNOWN, which evaluates to false in WHERE clauses. You must use 'IS NULL'.",
            "Beginner"
        ));

        questions.add(new QuizQuestion(
            3,
            "Aggregations & Grouping",
            "What is the key difference between WHERE and HAVING clauses in SQL?",
            Arrays.asList(
                "WHERE is for SELECT; HAVING is for INSERT",
                "WHERE filters rows before aggregation; HAVING filters aggregate group results",
                "WHERE supports aggregate functions like AVG(); HAVING does not",
                "There is no difference"
            ),
            1,
            "WHERE filters individual records BEFORE any grouping or aggregation takes place. HAVING filters grouped records AFTER aggregate functions like SUM() or AVG() are computed.",
            "Intermediate"
        ));

        questions.add(new QuizQuestion(
            4,
            "JOIN Behavior",
            "Given table 'students' with 8 rows (2 have NULL department_id) and table 'departments' with 4 rows, how many rows will 'SELECT * FROM students s JOIN departments d ON s.department_id = d.department_id' return?",
            Arrays.asList("8 rows", "6 rows", "4 rows", "10 rows"),
            1,
            "INNER JOIN matches rows where department_id equals department_id. The 2 students with NULL department_id do not match any department, so 8 - 2 = 6 rows are returned.",
            "Intermediate"
        ));

        questions.add(new QuizQuestion(
            5,
            "Aggregate Functions",
            "Which of the following queries is syntactically valid in SQL?",
            Arrays.asList(
                "SELECT department_id, AVG(marks) FROM students WHERE AVG(marks) > 80 GROUP BY department_id;",
                "SELECT department_id, AVG(marks) FROM students GROUP BY department_id HAVING AVG(marks) > 80;",
                "SELECT department_id, AVG(marks) FROM students HAVING AVG(marks) > 80;",
                "SELECT department_id FROM students WHERE HAVING marks > 80;"
            ),
            1,
            "Aggregate function filters (like AVG(marks) > 80) must be placed in a HAVING clause following a GROUP BY clause.",
            "Intermediate"
        ));

        questions.add(new QuizQuestion(
            6,
            "Subqueries",
            "What is a correlated subquery in SQL?",
            Arrays.asList(
                "A subquery that can be executed independently of the outer query",
                "A subquery that references columns from the outer query and evaluates once per row",
                "A subquery that only uses UNION operator",
                "A subquery that runs in a separate thread"
            ),
            1,
            "A correlated subquery references values from the outer query for each row processed. It depends on the outer query row context.",
            "Advanced"
        ));

        questions.add(new QuizQuestion(
            7,
            "NULL & COUNT()",
            "What does COUNT(department_id) return when evaluated on a table containing 5 rows where 2 rows have department_id = NULL?",
            Arrays.asList("5", "3", "NULL", "0"),
            1,
            "COUNT(column_name) ignores NULL values and counts only non-null values (5 - 2 = 3). Note that COUNT(*) counts all rows regardless of NULLs.",
            "Intermediate"
        ));

        questions.add(new QuizQuestion(
            8,
            "SQL Clause Execution Order",
            "What is the correct logical order of execution for standard SQL query clauses?",
            Arrays.asList(
                "SELECT -> FROM -> WHERE -> GROUP BY -> HAVING -> ORDER BY",
                "FROM -> WHERE -> GROUP BY -> HAVING -> SELECT -> ORDER BY",
                "FROM -> SELECT -> WHERE -> GROUP BY -> HAVING -> ORDER BY",
                "WHERE -> FROM -> GROUP BY -> SELECT -> ORDER BY"
            ),
            1,
            "SQL logical processing order starts with FROM (and JOINs), followed by WHERE, GROUP BY, HAVING, SELECT, and finally ORDER BY.",
            "Advanced"
        ));

        questions.add(new QuizQuestion(
            9,
            "DISTINCT Operator",
            "What does the SELECT DISTINCT clause do?",
            Arrays.asList(
                "Removes duplicate rows from the query output",
                "Sorts rows in ascending order",
                "Deletes duplicate rows from the database table",
                "Selects only foreign key columns"
            ),
            0,
            "SELECT DISTINCT filters out duplicate result rows so that only unique combinations of values are returned in the result set.",
            "Beginner"
        ));

        questions.add(new QuizQuestion(
            10,
            "Foreign Keys & Constraints",
            "What happens when a row in a parent table (departments) is deleted if a foreign key constraint has 'ON DELETE SET NULL'?",
            Arrays.asList(
                "Child table rows are deleted automatically",
                "Child table foreign key columns are updated to NULL",
                "The delete operation is rejected with an error",
                "Child table rows are copied to a backup table"
            ),
            1,
            "ON DELETE SET NULL automatically updates the referencing foreign key column in the child table to NULL when the parent record is deleted.",
            "Intermediate"
        ));
    }

    public List<QuizQuestion> getAllQuestions() {
        // Hide correct answer from question payload sent to frontend
        List<QuizQuestion> safeQuestions = new ArrayList<>();
        for (QuizQuestion q : questions) {
            safeQuestions.add(new QuizQuestion(
                q.getId(),
                q.getCategory(),
                q.getQuestion(),
                q.getOptions(),
                -1, // obfuscate index
                "", // obfuscate explanation until submitted
                q.getDifficulty()
            ));
        }
        return safeQuestions;
    }

    public QuizResult submitAnswer(QuizSubmission submission) {
        QuizQuestion q = questions.stream()
            .filter(item -> item.getId() == submission.getQuestionId())
            .findFirst()
            .orElse(null);

        if (q == null) {
            return new QuizResult(false, submission.getQuestionId(), submission.getSelectedOptionIndex(), -1, "Question not found.", 0.0);
        }

        boolean isCorrect = (submission.getSelectedOptionIndex() == q.getCorrectOptionIndex());

        // Log attempt into database
        logAttempt(submission.getQuestionId(), String.valueOf(submission.getSelectedOptionIndex()), isCorrect);

        double totalScore = calculateScorePercentage();

        return new QuizResult(
            isCorrect,
            q.getId(),
            submission.getSelectedOptionIndex(),
            q.getCorrectOptionIndex(),
            q.getExplanation(),
            totalScore
        );
    }

    private void logAttempt(int questionId, String answer, boolean isCorrect) {
        try {
            jdbcTemplate.update(
                "INSERT INTO quiz_attempts (question_id, user_answer, is_correct) VALUES (?, ?, ?)",
                questionId, answer, isCorrect
            );
        } catch (Exception e) {
            // Silently handle schema load
        }
    }

    public double calculateScorePercentage() {
        try {
            Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM quiz_attempts", Integer.class);
            if (total == null || total == 0) return 0.0;
            Integer correct = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM quiz_attempts WHERE is_correct = true", Integer.class);
            if (correct == null) correct = 0;
            return Math.round((double) correct / total * 100.0);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
