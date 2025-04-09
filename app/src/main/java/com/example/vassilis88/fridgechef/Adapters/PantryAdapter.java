package com.example.vassilis88.fridgechef.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.R;

import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private Context context;
    private List<String> ingredientList;

    private OnIngredientClickListener clickListener;

    public interface OnIngredientClickListener {
        void onIngredientClick(String ingredient, int position);
    }

    public PantryAdapter(Context context, List<String> ingredientList, OnIngredientClickListener clickListener) {
        this.context = context;
        this.ingredientList = ingredientList;
        this.clickListener = clickListener;
    }

    @Override
    public PantryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingredient, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PantryViewHolder holder, int position) {
        String ingredient = ingredientList.get(position);
        holder.nameTextView.setText(ingredient);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onIngredientClick(ingredient, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    public static class PantryViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;

        public PantryViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textView_ingredientName);

        }
    }
}
