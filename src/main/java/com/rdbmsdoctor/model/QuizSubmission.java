package com.rdbmsdoctor.model;

public class QuizSubmission {
    private int questionId;
    private int selectedOptionIndex;

    public QuizSubmission() {}

    public QuizSubmission(int questionId, int selectedOptionIndex) {
        this.questionId = questionId;
        this.selectedOptionIndex = selectedOptionIndex;
    }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public int getSelectedOptionIndex() { return selectedOptionIndex; }
    public void setSelectedOptionIndex(int selectedOptionIndex) { this.selectedOptionIndex = selectedOptionIndex; }
}
