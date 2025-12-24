package com.example.grader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Module extends AppCompatActivity {

    private ArrayList<Course> courseList;
    private CardAdapter adapter;
    private ActivityResultLauncher<Intent> editCardLauncher;

    private static final String PREFS_NAME = "com.example.grader.prefs";
    private static final String KEY_COURSES = "courses";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);

        loadData();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new CardAdapter(courseList);

        editCardLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Course course = (Course) data.getSerializableExtra(FormActivity.EXTRA_COURSE);
                        int position = data.getIntExtra("position", -1);

                        if (course != null) {
                            if (position != -1 && position < courseList.size()) {
                                courseList.set(position, course);
                                adapter.notifyItemChanged(position);
                            } else {
                                courseList.add(course);
                                adapter.notifyItemInserted(courseList.size() - 1);
                            }
                            saveData(); // Save data after adding/editing a course
                        }
                    }
                }
        );

        adapter.setOnAddClickListener(() -> {
            Intent intent = new Intent(Module.this, FormActivity.class);
            intent.putExtra("courseList", courseList);
            editCardLauncher.launch(intent);
        });

        adapter.setOnCardClickListener(position -> {
            Course clickedCourse = courseList.get(position);
            Intent intent = new Intent(Module.this, FormActivity.class);
            intent.putExtra(FormActivity.EXTRA_COURSE, clickedCourse);
            intent.putExtra("position", position);
            intent.putExtra("courseList", courseList);
            editCardLauncher.launch(intent);
        });

        adapter.setOnDeleteClickListener(position -> {
            courseList.remove(position);
            adapter.notifyItemRemoved(position);
            saveData(); // Save data after deleting a course
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(KEY_COURSES, null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Course>>() {}.getType();
        courseList = gson.fromJson(json, type);

        if (courseList == null) {
            courseList = new ArrayList<>();
        }
    }

    private void saveData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(courseList);
        editor.putString(KEY_COURSES, json);
        editor.apply();
    }
}
