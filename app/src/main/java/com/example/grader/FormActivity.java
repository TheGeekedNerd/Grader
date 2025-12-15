package com.example.grader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        // Find views
        inputTitle = findViewById(R.id.inputTitle);
        inputDescription = findViewById(R.id.inputDescription);
        btnSave = findViewById(R.id.btnSave);
        addAssessmentButton = findViewById(R.id.addAssessmentButton);
        assessmentsContainer = findViewById(R.id.assessmentsContainer);
        totalGradeTextView = findViewById(R.id.totalGradeTextView);

        addAssessmentButton.setOnClickListener(v -> addAssessmentRow());


        // Retrieve the incoming Intent to get existing data and position
        Intent incomingIntent = getIntent();

        // Pre-fill fields with existing data
        if (incomingIntent != null) {
            String oldTitle = incomingIntent.getStringExtra("title");
            String oldDesc = incomingIntent.getStringExtra("description");
            inputTitle.setText(oldTitle);
            inputDescription.setText(oldDesc);
        }

        // When save is clicked, send data back
        btnSave.setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();
            String desc = inputDescription.getText().toString().trim();

            // Return result
            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("description", desc);

            // Pass the original position back to the calling activity
            int position = incomingIntent.getIntExtra("position", -1);
            resultIntent.putExtra("position", position);

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void addAssessmentRow() {
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
                try {
                    String marks = s.toString();
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
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        deleteRowButton.setOnClickListener(v -> {
            assessmentsContainer.removeView(rowView);
            calculateTotalGrade();
        });

        assessmentsContainer.addView(rowView, assessmentsContainer.getChildCount());
    }

    private void calculateTotalGrade() {
        Map<String, List<Double>> categoryPercentages = new HashMap<>();
        Map<String, Double> categoryWeights = new HashMap<>();

        for (int i = 0; i < assessmentsContainer.getChildCount(); i++) {
            View rowView = assessmentsContainer.getChildAt(i);
            Spinner categorySpinner = rowView.findViewById(R.id.categorySpinner);
            TextView percentageTextView = rowView.findViewById(R.id.percentageTextView);
            EditText weightEditText = rowView.findViewById(R.id.weightEditText);

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
