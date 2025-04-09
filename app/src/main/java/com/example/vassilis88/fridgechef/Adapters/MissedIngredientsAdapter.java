package com.example.vassilis88.fridgechef.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.Models.MissedIngredient;
import com.example.vassilis88.fridgechef.R;

import java.util.List;

public class MissedIngredientsAdapter extends RecyclerView.Adapter<MissedIngredientsAdapter.MissedIngredientsViewHolder> {
    Context context;
    List<MissedIngredient> missedIngredients;

    public MissedIngredientsAdapter(Context context, List<MissedIngredient> missedIngredients) {
        this.context = context;
        this.missedIngredients = missedIngredients;
    }

    @NonNull
    @Override
    public MissedIngredientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MissedIngredientsViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_missed_ingredient, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MissedIngredientsViewHolder holder, int position) {
        MissedIngredient ingredient = missedIngredients.get(position);
        holder.textView_missedIngredientName.setText(ingredient.name);
    }

    @Override
    public int getItemCount() {
        return missedIngredients.size();
    }

    public static class MissedIngredientsViewHolder extends RecyclerView.ViewHolder {
        TextView textView_missedIngredientName;

        public MissedIngredientsViewHolder(@NonNull View itemView) {
            super(itemView);
            textView_missedIngredientName = itemView.findViewById(R.id.textView_missedIngredientName);
        }
    }
}
