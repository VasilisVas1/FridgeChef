package com.example.vassilis88.fridgechef.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.Models.Ingredient;
import com.example.vassilis88.fridgechef.R;

import java.util.List;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatsViewHolder> {

    private List<Ingredient> ingredients;

    public StatisticsAdapter(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public StatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient2, parent, false);
        return new StatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatsViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.name.setText(ingredient.name);
        holder.count.setText(" - Used by " + ingredient.count + " users");
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class StatsViewHolder extends RecyclerView.ViewHolder {
        TextView name, count;

        StatsViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textView_ingredientNameStats);  // Correct ID for name
            count = itemView.findViewById(R.id.textView_ingredientcountStats); // Correct ID for count
        }
    }
}

