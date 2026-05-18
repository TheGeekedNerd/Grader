package com.example.grader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FormActivity extends AppCompatActivity {

    private EditText inputTitle, inputDescription;
    private Button btnSave, addAssessmentButton;
    private LinearLayout assessmentsContainer;
    private TextView totalGradeTextView;
    private ArrayList<Course> courseList;
    private int currentCoursePosition = -1;

    public static final String EXTRA_COURSE = "com.example.grader.COURSE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        inputTitle = findViewById(R.id.inputTitle);
        inputDescription = findViewById(R.id.inputDescription);
        btnSave = findViewById(R.id.btnSave);
        addAssessmentButton = findViewById(R.id.addAssessmentButton);
        assessmentsContainer = findViewById(R.id.assessmentsContainer);
        totalGradeTextView = findViewById(R.id.totalGradeTextView);

        addAssessmentButton.setOnClickListener(v -> addAssessmentRow(null));

        Intent incomingIntent = getIntent();
        courseList = (ArrayList<Course>) incomingIntent.getSerializableExtra("courseList");
        if (courseList == null) {
            courseList = new ArrayList<>();
        }
        currentCoursePosition = incomingIntent.getIntExtra("position", -1);

        if (incomingIntent.hasExtra(EXTRA_COURSE)) {
            Course currentCourse = (Course) incomingIntent.getSerializableExtra(EXTRA_COURSE);
            if (currentCourse != null) {
                inputTitle.setText(currentCourse.getTitle());
                inputDescription.setText(currentCourse.getDescription());
                if (currentCourse.getAssessments() != null) {
                    for (Assessment assessment : currentCourse.getAssessments()) {
                        addAssessmentRow(assessment);
                    }
                }
            }
        }

        btnSave.setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim().toUpperCase();
            String desc = inputDescription.getText().toString().trim().toUpperCase();

            if (TextUtils.isEmpty(title)) {
                inputTitle.setError("Course name is required");
                return;
            }

            if (TextUtils.isEmpty(desc)) {
                inputDescription.setError("Course code is required");
                return;
            }

            for (int i = 0; i < courseList.size(); i++) {
                if (i == currentCoursePosition) {
                    continue;
                }
                Course course = courseList.get(i);
                if (course.getTitle().equals(title) || course.getDescription().equals(desc)) {
                    Toast.makeText(this, "A module with the same name or code already exists.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (!areAssessmentsValid()) {
                Toast.makeText(this, "Two assessments of the same category cannot have the same number", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Assessment> assessments = new ArrayList<>();

            for (int i = 0; i < assessmentsContainer.getChildCount(); i++) {
                View row = assessmentsContainer.getChildAt(i);

                Spinner categorySpinner = row.findViewById(R.id.categorySpinner);
                Spinner numberSpinner = row.findViewById(R.id.numberSpinner);
                EditText marksEditText = row.findViewById(R.id.marksEditText);
                EditText weightEditText = row.findViewById(R.id.weightEditText);
                EditText otherCategoryEditText = row.findViewById(R.id.otherCategoryEditText);

                String category = categorySpinner.getSelectedItem().toString();
                String otherCategoryName = null;
                if (category.equals("Other")) {
                    otherCategoryName = otherCategoryEditText.getText().toString();
                }
                int assessmentNumber = (int) numberSpinner.getSelectedItem();
                String marks = marksEditText.getText().toString();
                String weight = weightEditText.getText().toString();

                if (TextUtils.isEmpty(marks)) {
                    Toast.makeText(this, "Please enter marks for all assessments.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(weight)) {
                    Toast.makeText(this, "Please enter a weight for all assessments.", Toast.LENGTH_SHORT).show();
                    return;
                }

                assessments.add(new Assessment(category, marks, weight, assessmentNumber, otherCategoryName));
            }

            Course course = new Course(title, desc, assessments);

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_COURSE, course);
            resultIntent.putExtra("position", currentCoursePosition);

            setResult(RESULT_OK, resultIntent);
            finish();
        });

        calculateTotalGrade();
    }

    private void addAssessmentRow(Assessment assessment) {
        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.assessment_row, null);

        Spinner categorySpinner = rowView.findViewById(R.id.categorySpinner);
        Spinner numberSpinner = rowView.findViewById(R.id.numberSpinner);
        EditText marksEditText = rowView.findViewById(R.id.marksEditText);
        EditText weightEditText = rowView.findViewById(R.id.weightEditText);
        TextView percentageTextView = rowView.findViewById(R.id.percentageTextView);
        Button deleteRowButton = rowView.findViewById(R.id.deleteRowButton);
        EditText otherCategoryEditText = rowView.findViewById(R.id.otherCategoryEditText);

        ArrayAdapter<CharSequence> categoryAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.assessment_categories,
                        R.layout.spinner_item
                );
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            numbers.add(i);
        }
        ArrayAdapter<Integer> numberAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, numbers);
        numberAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        numberSpinner.setAdapter(numberAdapter);

        if (assessment != null) {
            marksEditText.setText(assessment.getMarks());
            weightEditText.setText(assessment.getWeight());

            updatePercentage(assessment.getMarks(), percentageTextView);

            for (int i = 0; i < categoryAdapter.getCount(); i++) {
                if (categoryAdapter.getItem(i).toString().equals(assessment.getCategory())) {
                    categorySpinner.setSelection(i);
                    if (assessment.getCategory().equals("Other")) {
                        otherCategoryEditText.setVisibility(View.VISIBLE);
                        otherCategoryEditText.setText(assessment.getOtherCategoryName());
                    }
                    break;
                }
            }

            numberSpinner.setSelection(assessment.getAssessmentNumber() - 1);
        }

        marksEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePercentage(s.toString(), percentageTextView);
                calculateTotalGrade();
            }
        });

        weightEditText.addTextChangedListener(new SimpleTextWatcher(this::calculateTotalGrade));

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                if (selectedCategory.equals("Other")) {
                    otherCategoryEditText.setVisibility(View.VISIBLE);
                } else {
                    otherCategoryEditText.setVisibility(View.GONE);
                }

                if (assessment == null) {
                    int nextNumber = getNextAssessmentNumber(selectedCategory, rowView);
                    if (nextNumber <= numberAdapter.getCount()) {
                        numberSpinner.setSelection(nextNumber - 1);
                    }
                }
                calculateTotalGrade();
            }
        });

        numberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateTotalGrade();
            }
        });

        deleteRowButton.setOnClickListener(v -> {
            assessmentsContainer.removeView(rowView);
            calculateTotalGrade();
        });

        assessmentsContainer.addView(rowView);
        if (assessment == null) {
            String selectedCategory = categorySpinner.getSelectedItem().toString();
            int nextNumber = getNextAssessmentNumber(selectedCategory, rowView);
            if (nextNumber <= numberAdapter.getCount()) {
                numberSpinner.setSelection(nextNumber - 1);
            }
        }
    }

    private void updatePercentage(String marks, TextView percentageTextView) {
        try {
            if (marks.contains("/")) {
                String[] parts = marks.split("/");
                double obtained = Double.parseDouble(parts[0].trim());
                double total = Double.parseDouble(parts[1].trim());
                if (total != 0) {
                    double percentage = (obtained / total) * 100;
                    // FIX: Use Locale.US to ensure consistent decimal formatting across devices
                    percentageTextView.setText(String.format(Locale.US, "%.2f%%", percentage));
                } else {
                    percentageTextView.setText("0%");
                }
            } else {
                percentageTextView.setText("0%");
            }
        } catch (Exception e) {
            percentageTextView.setText("0%");
        }
    }

    private void calculateTotalGrade() {
        double total = 0;
        // FIX: Check validity once before the loop, not inside it
        boolean isInvalid = !areAssessmentsValid();

        for (int i = 0; i < assessmentsContainer.getChildCount(); i++) {
            View row = assessmentsContainer.getChildAt(i);

            TextView percentageText = row.findViewById(R.id.percentageTextView);
            EditText weightEdit = row.findViewById(R.id.weightEditText);

            double percentage = 0;
            try {
                percentage = Double.parseDouble(
                        percentageText.getText().toString().replace("%", "")
                );
            } catch (NumberFormatException e) {
                // leave percentage as 0
            }

            double weight = 0;
            if (!weightEdit.getText().toString().isEmpty()) {
                try {
                    weight = Double.parseDouble(weightEdit.getText().toString());
                } catch (NumberFormatException e) {
                    // leave weight as 0
                }
            }

            // FIX: If any row is invalid, flag and stop accumulating
            if (percentage > 100 || weight > 100) {
                isInvalid = true;
                break;
            }

            total += (percentage / 100) * weight;
        }

        if (isInvalid) {
            total = 0;
        }

        // FIX: Use Locale.US to ensure consistent decimal formatting across devices
        totalGradeTextView.setText(String.format(Locale.US, "Total Grade: %.2f%%", total));
    }

    private int getNextAssessmentNumber(String category, View currentRow) {
        int maxNumber = 0;
        for (int i = 0; i < assessmentsContainer.getChildCount(); i++) {
            View row = assessmentsContainer.getChildAt(i);
            if (row == currentRow) continue;

            Spinner categorySpinner = row.findViewById(R.id.categorySpinner);
            Spinner numberSpinner = row.findViewById(R.id.numberSpinner);
            if (categorySpinner != null && categorySpinner.getSelectedItem() != null
                    && numberSpinner != null && numberSpinner.getSelectedItem() != null) {
                String currentCategory = categorySpinner.getSelectedItem().toString();
                if (currentCategory.equals("Other")) {
                    EditText otherCategoryEditText = row.findViewById(R.id.otherCategoryEditText);
                    currentCategory = otherCategoryEditText.getText().toString();
                }
                if (currentCategory.equals(category)) {
                    int currentNumber = (int) numberSpinner.getSelectedItem();
                    if (currentNumber > maxNumber) {
                        maxNumber = currentNumber;
                    }
                }
            }
        }
        return maxNumber + 1;
    }

    private boolean areAssessmentsValid() {
        Map<String, Set<Integer>> categoryNumbers = new HashMap<>();

        for (int i = 0; i < assessmentsContainer.getChildCount(); i++) {
            View row = assessmentsContainer.getChildAt(i);
            Spinner categorySpinner = row.findViewById(R.id.categorySpinner);
            Spinner numberSpinner = row.findViewById(R.id.numberSpinner);

            if (categorySpinner != null && categorySpinner.getSelectedItem() != null
                    && numberSpinner != null && numberSpinner.getSelectedItem() != null) {
                String category = categorySpinner.getSelectedItem().toString();
                if (category.equals("Other")) {
                    EditText otherCategoryEditText = row.findViewById(R.id.otherCategoryEditText);
                    category = otherCategoryEditText.getText().toString();
                }
                int number = (int) numberSpinner.getSelectedItem();

                if (!categoryNumbers.containsKey(category)) {
                    categoryNumbers.put(category, new HashSet<>());
                }

                if (!categoryNumbers.get(category).add(number)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable callback;
        SimpleTextWatcher(Runnable callback) { this.callback = callback; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            callback.run();
        }
    }
}