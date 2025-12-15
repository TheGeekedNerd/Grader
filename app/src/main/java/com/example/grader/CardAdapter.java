package com.example.grader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Two types of rows: a normal card OR the "Add" button card
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_ADD_BUTTON = 1;

    private ArrayList<CardItem> cardList;

    // Listener for when the Add button is clicked
    private OnAddClickListener addClickListener;

    // Listener for when a card itself is clicked
    private OnCardClickListener cardClickListener;

    // -------------------------------
    // Listener Interfaces
    // -------------------------------

    // For the bottom "Add Item" button
    public interface OnAddClickListener {
        void onAddClicked();
    }

    // For clicking a card
    public interface OnCardClickListener {
        void onCardClicked(int position);
    }

    // -------------------------------
    // Constructor
    // -------------------------------
    public CardAdapter(ArrayList<CardItem> cardList) {
        this.cardList = cardList;
    }

    // Set the Add button listener
    public void setOnAddClickListener(OnAddClickListener listener) {
        this.addClickListener = listener;
    }

    // Set the card click listener
    public void setOnCardClickListener(OnCardClickListener listener) {
        this.cardClickListener = listener;
    }

    // -------------------------------
    // RecyclerView Required Methods
    // -------------------------------

    @Override
    public int getItemViewType(int position) {
        // Last row (after all cards) = the Add button
        return (position == cardList.size()) ? TYPE_ADD_BUTTON : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        // +1 because we add the "Add" button card at the bottom
        return cardList.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_ADD_BUTTON) {
            // Inflate the Add button layout
            View view = inflater.inflate(R.layout.item_add_button, parent, false);
            return new AddViewHolder(view);
        } else {
            // Inflate normal card layout
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
            // Normal card item
            CardItem item = cardList.get(position);
            ItemViewHolder vh = (ItemViewHolder) holder;

            vh.title.setText(item.getTitle());
            vh.description.setText(item.getDescription());
        }
    }

    // -------------------------------
    // ViewHolder for Normal Card
    // -------------------------------
    class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView title, description;
        Button deleteBtn;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.cardTitle);
            description = itemView.findViewById(R.id.cardDescription);
            deleteBtn = itemView.findViewById(R.id.btnDelete);

            // Delete button removes the item from the list
            deleteBtn.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    cardList.remove(pos);
                    notifyItemRemoved(pos);
                }
            });

            // Clicking the card opens the form/activity
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

            // Clicking Add triggers the listener in Module.java
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
    public void addItem(CardItem item) {
        cardList.add(item);
        notifyItemInserted(cardList.size() - 1);
    }
}
