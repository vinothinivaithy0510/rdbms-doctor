package com.rdbmsdoctor.model;

import java.util.List;

public class QuizQuestion {
    private int id;
    private String category;
    private String question;
    private List<String> options;
    private int correctOptionIndex;
    private String explanation;
    private String difficulty;

    public QuizQuestion() {}

    public QuizQuestion(int id, String category, String question, List<String> options, int correctOptionIndex, String explanation, String difficulty) {
        this.id = id;
        this.category = category;
        this.question = question;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation;
        this.difficulty = difficulty;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectOptionIndex() { return correctOptionIndex; }
    public void setCorrectOptionIndex(int correctOptionIndex) { this.correctOptionIndex = correctOptionIndex; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}
