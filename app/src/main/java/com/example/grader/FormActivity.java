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

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class FormActivity extends AppCompatActivity {

    private EditText inputTitle, inputDescription;
    private Button btnSave, addAssessmentButton;
    private LinearLayout assessmentsContainer;
    private TextView totalGradeTextView;

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
        if (incomingIntent != null && incomingIntent.hasExtra(EXTRA_COURSE)) {
            Course course = (Course) incomingIntent.getSerializableExtra(EXTRA_COURSE);
            if (course != null) {
                inputTitle.setText(course.getTitle());
                inputDescription.setText(course.getDescription());
                if (course.getAssessments() != null) {
                    for (Assessment assessment : course.getAssessments()) {
                        addAssessmentRow(assessment);
                    }
                }
            }
        }

        btnSave.setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();
            String desc = inputDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                inputTitle.setError("Course name is required");
                return;
            }

            if (TextUtils.isEmpty(desc)) {
                inputDescription.setError("Course code is required");
                return;
            }

            List<Assessment> assessments = new ArrayList<>();

            for (int i = 0; i < assessmentsContainer.getChildCount(); i++) {
                View row = assessmentsContainer.getChildAt(i);

                Spinner categorySpinner = row.findViewById(R.id.categorySpinner);
                Spinner numberSpinner = row.findViewById(R.id.numberSpinner);
                EditText marksEditText = row.findViewById(R.id.marksEditText);
                EditText weightEditText = row.findViewById(R.id.weightEditText);

                String category = categorySpinner.getSelectedItem().toString();
                int assessmentNumber = (int) numberSpinner.getSelectedItem();
                String marks = marksEditText.getText().toString();
                String weight = weightEditText.getText().toString();

                assessments.add(new Assessment(category, marks, weight, assessmentNumber));
            }

            Course course = new Course(title, desc, assessments);

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_COURSE, course);
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
                calculateTotalGrade();
            }
        });

        numberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) {}
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // No action needed on selection, will be saved on "Save" button click.
            }
        });

        deleteRowButton.setOnClickListener(v -> {
            assessmentsContainer.removeView(rowView);
            calculateTotalGrade();
        });

        assessmentsContainer.addView(rowView);
    }

    private void updatePercentage(String marks, TextView percentageTextView) {
        try {
            if (marks.contains("/")) {
                String[] parts = marks.split("/");
                double obtained = Double.parseDouble(parts[0].trim());
                double total = Double.parseDouble(parts[1].trim());
                if (total != 0) {
                    double percentage = (obtained / total) * 100;
                    percentageTextView.setText(String.format("%.2f%%", percentage));
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
        boolean isInvalid = false;

        for (int i = 0; i < assessmentsContainer.getChildCount(); i++) {
            View row = assessmentsContainer.getChildAt(i);

            TextView percentageText = row.findViewById(R.id.percentageTextView);
            EditText weightEdit = row.findViewById(R.id.weightEditText);

            double percentage = 0;
            try {
                percentage = Double.parseDouble(percentageText.getText().toString().replace("%", ""));
            } catch (NumberFormatException e) {
                // Ignore if the percentage is not a valid number
            }

            double weight = 0;
            if (!weightEdit.getText().toString().isEmpty()) {
                try {
                    weight = Double.parseDouble(weightEdit.getText().toString());
                } catch (NumberFormatException e) {
                    // Ignore if the weight is not a valid number
                }
            }

            if (percentage > 100 || weight > 100) {
                isInvalid = true;
                break;
            }

            total += (percentage / 100) * weight;
        }

        if (isInvalid) {
            total = 0;
        }

        totalGradeTextView.setText(String.format("Total Grade: %.2f%%", total));
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
