package com.example.grader;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher; // New Import
import androidx.activity.result.contract.ActivityResultContracts; // New Import
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Module extends AppCompatActivity {

    private ArrayList<CardItem> cardList;
    private CardAdapter adapter;

    // Declare the launcher to handle results from FormActivity
    private ActivityResultLauncher<Intent> editCardLauncher; // New Declaration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // Create empty list for the cards
        cardList = new ArrayList<>();

        // Initialize adapter
        adapter = new CardAdapter(cardList);

        // 1. Initialize the Activity Result Launcher
        editCardLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Check if the result is OK (saved successfully)
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        // Retrieve updated data and position sent from FormActivity
                        String newTitle = data.getStringExtra("title");
                        String newDesc = data.getStringExtra("description");
                        int position = data.getIntExtra("position", -1);

                        // Update the specific card in the list
                        if (position != -1 && position < cardList.size()) {
                            CardItem item = cardList.get(position);
                            item.setTitle(newTitle); // Use the new setter method
                            item.setDescription(newDesc); // Use the new setter method

                            // Notify the adapter to refresh only the updated item's view
                            adapter.notifyItemChanged(position);
                        }
                    }
                }
        );

        // ---------------------------
        // Add button clicked (bottom button in recyclerview)
        // ---------------------------
        adapter.setOnAddClickListener(() -> {
            // Add a new card with default title + description
            CardItem newCard = new CardItem("", "");
            adapter.addItem(newCard);
        });

        // ---------------------------
        // Card clicked → open form page
        // ---------------------------
        adapter.setOnCardClickListener(position -> {
            CardItem clickedCard = cardList.get(position);

            // Open the form activity (you must create FormActivity)
            Intent intent = new Intent(Module.this, FormActivity.class);

            // Pass card details into form screen
            intent.putExtra("title", clickedCard.getTitle());
            intent.putExtra("description", clickedCard.getDescription());
            intent.putExtra("position", position); // Pass position for updating

            // Use the LAUNCHER instead of startActivity
            editCardLauncher.launch(intent);
        });

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}