package com.example.vassilis88.fridgechef.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.R;

import java.util.List;

public class StatisticsUserIngredientsAdapter extends RecyclerView.Adapter<StatisticsUserIngredientsAdapter.ViewHolder> {

    private List<String> userIngredients;

    public StatisticsUserIngredientsAdapter(List<String> userIngredients) {
        this.userIngredients = userIngredients;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ingredientNameTextView = itemView.findViewById(R.id.textView_ingredientName);
        }
    }

    @NonNull
    @Override
    public StatisticsUserIngredientsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticsUserIngredientsAdapter.ViewHolder holder, int position) {
        String ingredient = userIngredients.get(position);
        holder.ingredientNameTextView.setText(ingredient);
    }

    @Override
    public int getItemCount() {
        return userIngredients.size();
    }
}
