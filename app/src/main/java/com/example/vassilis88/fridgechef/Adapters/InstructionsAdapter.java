package com.example.vassilis88.fridgechef.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.Models.InstructionsResponse;
import com.example.vassilis88.fridgechef.Models.Step;
import com.example.vassilis88.fridgechef.R;

import java.util.List;

import kotlin.jvm.internal.Lambda;

public class InstructionsAdapter extends RecyclerView.Adapter<InstructionsViewHolder>{
    Context context;
    List<InstructionsResponse> list;

    public InstructionsAdapter(Context context, List<InstructionsResponse> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public InstructionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InstructionsViewHolder(LayoutInflater.from(context).inflate(R.layout.list_instructions,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull InstructionsViewHolder holder, int position) {
        holder.textView_instruction_name.setText(list.get(position).name);
        holder.recycler_instruction_steps.setHasFixedSize(true);
        holder.recycler_instruction_steps.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false));

        InstructionStepAdapter stepAdapter= new InstructionStepAdapter(context,list.get(position).steps);
        holder.recycler_instruction_steps.setAdapter(stepAdapter);



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // Method to return the instructions as a formatted string for HTML
    public String getInstructionsAsHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<ul>");  // Or <ol> for an ordered list of instructions
        for (InstructionsResponse instruction : list) {
            html.append("<li><strong>").append(instruction.name).append("</strong><ul>");
            for (Step step : instruction.steps) {
                // Add each step as a list item
                html.append("<li>").append(step.step).append("</li>");
            }
            html.append("</ul></li>");
        }
        html.append("</ul>");
        return html.toString();
    }

}

//View Holder Class
class InstructionsViewHolder extends RecyclerView.ViewHolder{


    TextView textView_instruction_name;
    RecyclerView recycler_instruction_steps;
    public InstructionsViewHolder(@NonNull View itemView) {
        super(itemView);
        textView_instruction_name=itemView.findViewById(R.id.textView_instruction_name);
        recycler_instruction_steps=itemView.findViewById(R.id.recycler_instruction_steps);

    }
}
