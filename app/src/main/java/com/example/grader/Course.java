package com.example.grader;

import java.io.Serializable;
import java.util.List;

public class Course implements Serializable {
    private String title;
    private String description;
    private List<Assessment> assessments;

    public Course(String title, String description, List<Assessment> assessments) {
        this.title = title;
        this.description = description;
        this.assessments = assessments;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Assessment> getAssessments() {
        return assessments;
    }
}
