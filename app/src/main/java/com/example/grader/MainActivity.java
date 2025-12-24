package com.example.grader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button add_modules;
    private List<Course> courses = new ArrayList<>(); // Assuming you have a list of courses

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        add_modules = findViewById(R.id.module);
        add_modules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Module.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Course newCourse = (Course) data.getSerializableExtra(FormActivity.EXTRA_COURSE);
            if (newCourse != null) {
                boolean isDuplicate = false;
                for (Course course : courses) {
                    if (course.getTitle().equals(newCourse.getTitle()) && course.getDescription().equals(newCourse.getDescription())) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (isDuplicate) {
                    Toast.makeText(this, "A module with the same name and code already exists.", Toast.LENGTH_SHORT).show();
                } else {
                    courses.add(newCourse);
                    // Update your UI to reflect the new course
                }
            }
        }
    }
}