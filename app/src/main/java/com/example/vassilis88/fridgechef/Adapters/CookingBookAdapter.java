package com.example.vassilis88.fridgechef.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vassilis88.fridgechef.Listenerts.RecipeDetailsListener;
import com.example.vassilis88.fridgechef.Models.RecipeDetailsResponse;
import com.example.vassilis88.fridgechef.R;
import com.example.vassilis88.fridgechef.RecipeDetailsActivity;
import com.example.vassilis88.fridgechef.RequestManager;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CookingBookAdapter extends RecyclerView.Adapter<CookingBookAdapter.ViewHolder> {

    Context context;
    List<String> recipeIds;
    RequestManager manager;

    public CookingBookAdapter(Context context, List<String> recipeIds) {
        this.context = context;
        this.recipeIds = recipeIds;
        this.manager = new RequestManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_savedrecipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String recipeId = recipeIds.get(position);

        manager.getRecipeDetails(new RecipeDetailsListener() {
            @Override
            public void didFetch(RecipeDetailsResponse response, String message) {
                holder.textViewRecipeName.setText(response.title);
                Picasso.get().load(response.image).into(holder.imageViewRecipe);

                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, RecipeDetailsActivity.class);
                    intent.putExtra("id", response.id + "");
                    context.startActivity(intent);
                });
            }

            @Override
            public void didError(String message) {
                Toast.makeText(context, "Error loading recipe", Toast.LENGTH_SHORT).show();
            }
        }, Integer.parseInt(recipeId));
    }

    @Override
    public int getItemCount() {
        return recipeIds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRecipeName;
        ImageView imageViewRecipe;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRecipeName = itemView.findViewById(R.id.textView_titleSavedRecipe);
            imageViewRecipe = itemView.findViewById(R.id.imageView_savedRecipe);
        }
    }
}

