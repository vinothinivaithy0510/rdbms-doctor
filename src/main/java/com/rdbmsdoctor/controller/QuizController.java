package com.rdbmsdoctor.controller;

import com.rdbmsdoctor.model.QuizQuestion;
import com.rdbmsdoctor.model.QuizResult;
import com.rdbmsdoctor.model.QuizSubmission;
import com.rdbmsdoctor.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    @Autowired
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/questions")
    public List<QuizQuestion> getQuestions() {
        return quizService.getAllQuestions();
    }

    @PostMapping("/submit")
    public QuizResult submitAnswer(@RequestBody QuizSubmission submission) {
        return quizService.submitAnswer(submission);
    }
}
