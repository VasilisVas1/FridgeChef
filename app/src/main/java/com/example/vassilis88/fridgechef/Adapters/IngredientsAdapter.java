package com.example.vassilis88.fridgechef.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.Models.ExtendedIngredient;
import com.example.vassilis88.fridgechef.R;

import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsViewHolder> {
    Context context;
    List<ExtendedIngredient> list;

    public IngredientsAdapter(Context context, List<ExtendedIngredient> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public IngredientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IngredientsViewHolder(LayoutInflater.from(context).inflate(R.layout.list_meal_ingredients,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientsViewHolder holder, int position) {
        holder.textView_ingredients_quantity.setText(list.get(position).original);
        holder.textView_ingredients_quantity.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public String getIngredientsAsHtml() {
        StringBuilder html = new StringBuilder();
        for (ExtendedIngredient ingredient : list) {
            html.append("<li>").append(ingredient.original).append("</li>");
        }
        return html.toString();
    }

    public String getIngredientsAsText() {
        StringBuilder ingredientsText = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            ExtendedIngredient ingredient = list.get(i);
            if (i > 0) {
                ingredientsText.append(", ");
            }
            if (ingredient.original != null && !ingredient.original.isEmpty()) {
                ingredientsText.append(ingredient.original);
            }
        }
        return ingredientsText.toString();
    }
}

class IngredientsViewHolder extends RecyclerView.ViewHolder{
    TextView textView_ingredients_quantity;

    public IngredientsViewHolder(@NonNull View itemView) {
        super(itemView);
        textView_ingredients_quantity=itemView.findViewById(R.id.textView_ingredients_quantity);
    }
}
