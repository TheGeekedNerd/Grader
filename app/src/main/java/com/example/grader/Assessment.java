package com.example.grader;

import java.io.Serializable;

public class Assessment implements Serializable {
    private String category;
    private String marks;
    private String weight;
    private int assessmentNumber;
    private String otherCategoryName;

    public Assessment(String category, String marks, String weight, int assessmentNumber, String otherCategoryName) {
        this.category = category;
        this.marks = marks;
        this.weight = weight;
        this.assessmentNumber = assessmentNumber;
        this.otherCategoryName = otherCategoryName;
    }

    public String getCategory() {
        return category;
    }

    public String getMarks() {
        return marks;
    }

    public String getWeight() {
        return weight;
    }

    public int getAssessmentNumber() {
        return assessmentNumber;
    }

    public String getOtherCategoryName() {
        return otherCategoryName;
    }
}
