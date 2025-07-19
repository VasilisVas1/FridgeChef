package com.example.vassilis88.fridgechef;

import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class NutritionAnalysisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.cream_dark));

        setContentView(R.layout.activity_nutrition_analysis);
        String nutritionAnalysis = getIntent().getStringExtra("analysis");
        String recipeName = getIntent().getStringExtra("recipeName");

        TextView titleTextView = findViewById(R.id.textView_nutrition_title);
        TextView analysisTextView = findViewById(R.id.textView_nutrition_analysis);

        titleTextView.setText("Nutrition Analysis: " + recipeName);
        analysisTextView.setText(nutritionAnalysis);
    }
}