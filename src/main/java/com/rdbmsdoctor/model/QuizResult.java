package com.rdbmsdoctor.model;

public class QuizResult {
    private boolean correct;
    private int questionId;
    private int selectedOptionIndex;
    private int correctOptionIndex;
    private String explanation;
    private double currentScorePercentage;

    public QuizResult() {}

    public QuizResult(boolean correct, int questionId, int selectedOptionIndex, int correctOptionIndex, String explanation, double currentScorePercentage) {
        this.correct = correct;
        this.questionId = questionId;
        this.selectedOptionIndex = selectedOptionIndex;
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation;
        this.currentScorePercentage = currentScorePercentage;
    }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public int getSelectedOptionIndex() { return selectedOptionIndex; }
    public void setSelectedOptionIndex(int selectedOptionIndex) { this.selectedOptionIndex = selectedOptionIndex; }

    public int getCorrectOptionIndex() { return correctOptionIndex; }
    public void setCorrectOptionIndex(int correctOptionIndex) { this.correctOptionIndex = correctOptionIndex; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public double getCurrentScorePercentage() { return currentScorePercentage; }
    public void setCurrentScorePercentage(double currentScorePercentage) { this.currentScorePercentage = currentScorePercentage; }
}
