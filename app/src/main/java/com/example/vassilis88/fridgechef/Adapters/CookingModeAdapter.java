package com.example.vassilis88.fridgechef.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.Models.Step;
import com.example.vassilis88.fridgechef.R;

import java.util.List;

public class CookingModeAdapter extends RecyclerView.Adapter<CookingModeAdapter.CookingModeViewHolder> {

    Context context;
    List<Step> steps;

    public CookingModeAdapter(Context context, List<Step> steps) {
        this.context = context;
        this.steps = steps;
    }

    @NonNull
    @Override
    public CookingModeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cooking_mode_step_item, parent, false);
        return new CookingModeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CookingModeViewHolder holder, int position) {
        Step step = steps.get(position);
        holder.textViewStepNumber.setText(String.format("Step %d", step.number));
        holder.textViewStepDescription.setText(step.step);
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    public static class CookingModeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewStepNumber, textViewStepDescription;

        public CookingModeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStepNumber = itemView.findViewById(R.id.textView_step_number);
            textViewStepDescription = itemView.findViewById(R.id.textView_step_description);
        }
    }
}
