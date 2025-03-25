package com.example.project_terrarium_prm392.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private OnCategoryClickListener listener;
    private int selectedPosition = 0; // Position 0 is for "All" category

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        if (position == 0) {
            // Special case for "All" category
            holder.textViewCategory.setText("All");
        } else {
            Category category = categories.get(position - 1);
            holder.textViewCategory.setText(category.getName());
        }

        // Set selected state
        holder.textViewCategory.setSelected(position == selectedPosition);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition != position) {
                int previousSelected = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);

                if (position == 0) {
                    // Special case for "All" category
                    listener.onCategoryClick(null, position);
                } else {
                    listener.onCategoryClick(categories.get(position - 1), position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        // Add 1 for "All" category
        return categories.size() + 1;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }
    
    public List<Category> getCategories() {
        return categories;
    }

    public void selectCategory(int position) {
        if (position >= 0 && position < getItemCount() && position != selectedPosition) {
            int previousSelected = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCategory;

        CategoryViewHolder(View itemView) {
            super(itemView);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
        }
    }
} 