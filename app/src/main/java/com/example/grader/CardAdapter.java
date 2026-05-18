package com.example.grader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Two types of rows: a normal card OR the "Add" button card
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_ADD_BUTTON = 1;

    private ArrayList<Course> courseList;

    // Listener for when the Add button is clicked
    private OnAddClickListener addClickListener;

    // Listener for when a card itself is clicked
    private OnCardClickListener cardClickListener;

    private OnDeleteClickListener deleteClickListener;

    // -------------------------------
    // Listener Interfaces
    // -------------------------------

    public interface OnAddClickListener {
        void onAddClicked();
    }

    public interface OnCardClickListener {
        void onCardClicked(int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClicked(int position);
    }

    // -------------------------------
    // Constructor
    // -------------------------------
    public CardAdapter(ArrayList<Course> courseList) {
        this.courseList = courseList;
    }

    public void setOnAddClickListener(OnAddClickListener listener) {
        this.addClickListener = listener;
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        this.cardClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    // -------------------------------
    // RecyclerView Required Methods
    // -------------------------------

    @Override
    public int getItemViewType(int position) {
        return (position == courseList.size()) ? TYPE_ADD_BUTTON : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return courseList.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_ADD_BUTTON) {
            View view = inflater.inflate(R.layout.item_add_button, parent, false);
            return new AddViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_card, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {
        if (holder.getItemViewType() == TYPE_ITEM) {
            Course item = courseList.get(position);
            ItemViewHolder vh = (ItemViewHolder) holder;

            vh.title.setText(item.getTitle());
            vh.description.setText(item.getDescription());
            vh.totalGrade.setText(String.format(Locale.US, "Total Grade: %.2f%%", calculateTotalGrade(item)));
        }
    }

    // -------------------------------
    // Total Grade Calculation
    // -------------------------------
    private double calculateTotalGrade(Course course) {
        List<Assessment> assessments = course.getAssessments();
        if (assessments == null || assessments.isEmpty()) return 0;

        double total = 0;
        for (Assessment assessment : assessments) {
            try {
                String marks = assessment.getMarks();
                double weight = Double.parseDouble(assessment.getWeight());

                if (marks.contains("/")) {
                    String[] parts = marks.split("/");
                    double obtained = Double.parseDouble(parts[0].trim());
                    double outOf = Double.parseDouble(parts[1].trim());
                    if (outOf != 0 && weight <= 100) {
                        double percentage = (obtained / outOf) * 100;
                        total += (percentage / 100) * weight;
                    }
                }
            } catch (Exception e) {
                // skip malformed assessment
            }
        }
        return total;
    }

    // -------------------------------
    // ViewHolder for Normal Card
    // -------------------------------
    class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView title, description, totalGrade;
        Button deleteBtn;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.cardTitle);
            description = itemView.findViewById(R.id.cardDescription);
            totalGrade = itemView.findViewById(R.id.cardTotalGrade);
            deleteBtn = itemView.findViewById(R.id.btnDelete);

            deleteBtn.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && deleteClickListener != null) {
                    deleteClickListener.onDeleteClicked(pos);
                }
            });

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && cardClickListener != null) {
                    cardClickListener.onCardClicked(pos);
                }
            });
        }
    }

    // -------------------------------
    // ViewHolder for Add Button
    // -------------------------------
    class AddViewHolder extends RecyclerView.ViewHolder {

        Button addBtn;

        public AddViewHolder(@NonNull View itemView) {
            super(itemView);

            addBtn = itemView.findViewById(R.id.btnAddItem);

            addBtn.setOnClickListener(v -> {
                if (addClickListener != null) {
                    addClickListener.onAddClicked();
                }
            });
        }
    }

    // -------------------------------
    // Add new card externally
    // -------------------------------
    public void addItem(Course item) {
        courseList.add(item);
        notifyItemInserted(courseList.size() - 1);
    }
}