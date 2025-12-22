package com.example.grader;

import java.io.Serializable;

public class Assessment implements Serializable {
    private String category;
    private String marks;
    private String weight;
    private int assessmentNumber;

    public Assessment(String category, String marks, String weight, int assessmentNumber) {
        this.category = category;
        this.marks = marks;
        this.weight = weight;
        this.assessmentNumber = assessmentNumber;
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
}
