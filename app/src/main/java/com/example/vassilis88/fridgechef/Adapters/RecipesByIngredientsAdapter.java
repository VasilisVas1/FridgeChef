package com.example.vassilis88.fridgechef.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.Listenerts.RecipeClickListener;
import com.example.vassilis88.fridgechef.Models.RecipesByIngredientsResponse;
import com.example.vassilis88.fridgechef.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipesByIngredientsAdapter extends RecyclerView.Adapter<RecipesByIngredientsViewHolder>{
    Context context;
    List<RecipesByIngredientsResponse> list;
    RecipeClickListener listener;

    public RecipesByIngredientsAdapter(Context context, List<RecipesByIngredientsResponse> list,RecipeClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener=listener;
    }

    @NonNull
    @Override
    public RecipesByIngredientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecipesByIngredientsViewHolder(LayoutInflater.from(context).inflate(R.layout.list_recipebyingredient,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecipesByIngredientsViewHolder holder, int position) {
        RecipesByIngredientsResponse recipe = list.get(position);

        holder.textView_titleRecipeByIngredient.setText(list.get(position).title);
        holder.textView_titleRecipeByIngredient.setSelected(true);
        holder.textView_numberofMissedIngredients.setText("Missed Ingredients: " + String.valueOf(list.get(position).missedIngredientCount));
        holder.textView_numberofMissedIngredients.setSelected(true);
        Picasso.get().load(list.get(position).image).into(holder.imageView_foodRecipeByIngredient);

        if (recipe.missedIngredients != null && !recipe.missedIngredients.isEmpty()) {
            holder.recycler_missedIngredients.setVisibility(View.VISIBLE);
            MissedIngredientsAdapter missedIngredientsAdapter = new MissedIngredientsAdapter(context, recipe.missedIngredients);
            holder.recycler_missedIngredients.setAdapter(missedIngredientsAdapter);
            holder.recycler_missedIngredients.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        } else {
            holder.recycler_missedIngredients.setVisibility(View.GONE);
        }

        holder.recipesByIngredients_list_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecipeClicked(String.valueOf(list.get(holder.getAdapterPosition()).id));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setRecipes(List<RecipesByIngredientsResponse> newList) {
        this.list = newList;
    }

}


class RecipesByIngredientsViewHolder extends RecyclerView.ViewHolder{
    CardView recipesByIngredients_list_container;
    TextView textView_titleRecipeByIngredient,textView_numberofMissedIngredients;
    ImageView imageView_foodRecipeByIngredient;

    RecyclerView recycler_missedIngredients;

    public RecipesByIngredientsViewHolder(@NonNull View itemView) {
        super(itemView);
        recipesByIngredients_list_container=itemView.findViewById(R.id.recipesByIngredients_list_container);
        textView_titleRecipeByIngredient=itemView.findViewById(R.id.textView_titleRecipeByIngredient);
        imageView_foodRecipeByIngredient=itemView.findViewById(R.id.imageView_foodRecipeByIngredient);
        textView_numberofMissedIngredients=itemView.findViewById(R.id.textView_numberofMissedIngredients);
        recycler_missedIngredients=itemView.findViewById(R.id.recycler_missedIngredients);
    }
}
