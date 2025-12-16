package com.example.grader;

import java.io.Serializable;

public class Assessment implements Serializable {
    private String category;
    private String marks;
    private String weight;

    public Assessment(String category, String marks, String weight) {
        this.category = category;
        this.marks = marks;
        this.weight = weight;
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
}
