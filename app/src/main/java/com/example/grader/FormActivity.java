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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormActivity extends AppCompatActivity {

    private EditText inputTitle, inputDescription;
    private Button btnSave;
    private Button addAssessmentButton;
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
        } else if (incomingIntent != null) { // for backwards compatibility
            String oldTitle = incomingIntent.getStringExtra("title");
            String oldDesc = incomingIntent.getStringExtra("description");
            inputTitle.setText(oldTitle);
            inputDescription.setText(oldDesc);
        }

        btnSave.setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();
            String desc = inputDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                inputTitle.setError("Course name is required.");
                return;
            }

            if (TextUtils.isEmpty(desc)) {
                inputDescription.setError("Course code is required.");
                return;
            }

            List<Assessment> assessments = new ArrayList<>();
            for (int i = 0; i < assessmentsContainer.getChildCount(); i++) {
                View rowView = assessmentsContainer.getChildAt(i);
                Spinner categorySpinner = rowView.findViewById(R.id.categorySpinner);
                EditText marksEditText = rowView.findViewById(R.id.marksEditText);
                EditText weightEditText = rowView.findViewById(R.id.weightEditText);

                if (categorySpinner.getSelectedItem() != null) {
                    String category = categorySpinner.getSelectedItem().toString();
                    String marks = marksEditText.getText().toString();
                    String weight = weightEditText.getText().toString();
                    assessments.add(new Assessment(category, marks, weight));
                }
            }

            Course course = new Course(title, desc, assessments);

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_COURSE, course);

            int position = incomingIntent.getIntExtra("position", -1);
            resultIntent.putExtra("position", position);

            setResult(RESULT_OK, resultIntent);
            finish();
        });

        calculateTotalGrade();
    }

    private void addAssessmentRow(Assessment assessment) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.assessment_row, null);

        Spinner categorySpinner = rowView.findViewById(R.id.categorySpinner);
        EditText marksEditText = rowView.findViewById(R.id.marksEditText);
        TextView percentageTextView = rowView.findViewById(R.id.percentageTextView);
        EditText weightEditText = rowView.findViewById(R.id.weightEditText);
        Button deleteRowButton = rowView.findViewById(R.id.deleteRowButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.assessment_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        if (assessment != null) {
            marksEditText.setText(assessment.getMarks());
            updatePercentage(marksEditText.getText().toString(), percentageTextView);
            weightEditText.setText(assessment.getWeight());
            if (assessment.getCategory() != null) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (adapter.getItem(i).toString().equals(assessment.getCategory())) {
                        categorySpinner.setSelection(i);
                        break;
                    }
                }
            }
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalGrade();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        marksEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePercentage(s.toString(), percentageTextView);
                calculateTotalGrade();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        weightEditText.addTextChangedListener(textWatcher);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculateTotalGrade();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        deleteRowButton.setOnClickListener(v -> {
            assessmentsContainer.removeView(rowView);
            calculateTotalGrade();
        });

        assessmentsContainer.addView(rowView, assessmentsContainer.getChildCount());
    }

    private void updatePercentage(String marks, TextView percentageTextView) {
        try {
            if (marks.contains("/")) {
                String[] parts = marks.split("/");
                if (parts.length == 2) {
                    double obtained = Double.parseDouble(parts[0].trim());
                    double total = Double.parseDouble(parts[1].trim());
                    if (total != 0) {
                        double percentage = (obtained / total) * 100;
                        percentageTextView.setText(String.format("%.2f%%", percentage));
                    } else {
                        percentageTextView.setText("0%");
                    }
                }
            } else {
                percentageTextView.setText("0%");
            }
        } catch (NumberFormatException e) {
            percentageTextView.setText("0%");
        }
    }

    private void calculateTotalGrade() {
        Map<String, List<Double>> categoryPercentages = new HashMap<>();
        Map<String, Double> categoryWeights = new HashMap<>();

        for (int i = 0; i < assessmentsContainer.getChildCount(); i++) {
            View rowView = assessmentsContainer.getChildAt(i);
            Spinner categorySpinner = rowView.findViewById(R.id.categorySpinner);
            TextView percentageTextView = rowView.findViewById(R.id.percentageTextView);
            EditText weightEditText = rowView.findViewById(R.id.weightEditText);

            if (categorySpinner.getSelectedItem() == null) continue;

            String category = categorySpinner.getSelectedItem().toString();
            String percentageString = percentageTextView.getText().toString().replace("%", "");
            String weightString = weightEditText.getText().toString();

            double percentage = 0;
            try {
                percentage = Double.parseDouble(percentageString);
            } catch (NumberFormatException e) { /* ignore */ }

            double weight = 0;
            try {
                weight = Double.parseDouble(weightString);
            } catch (NumberFormatException e) { /* ignore */ }

            if (!categoryPercentages.containsKey(category)) {
                categoryPercentages.put(category, new ArrayList<>());
            }
            categoryPercentages.get(category).add(percentage);
            categoryWeights.put(category, weight);
        }

        double totalGrade = 0;

        for (String category : categoryWeights.keySet()) {
            List<Double> percentages = categoryPercentages.get(category);
            double weight = categoryWeights.get(category);

            double sumOfPercentages = 0;
            for (double p : percentages) {
                sumOfPercentages += p;
            }
            double averagePercentage = percentages.isEmpty() ? 0 : sumOfPercentages / percentages.size();

            totalGrade += (averagePercentage / 100.0) * weight;
        }

        totalGradeTextView.setText(String.format("Total Grade: %.2f%%", totalGrade));
    }
}
