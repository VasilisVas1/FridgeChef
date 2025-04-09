package com.example.vassilis88.fridgechef;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.Adapters.CookingModeAdapter;
import com.example.vassilis88.fridgechef.Adapters.InstructionsAdapter;
import com.example.vassilis88.fridgechef.Listenerts.InstructionsListener;
import com.example.vassilis88.fridgechef.Models.InstructionsResponse;

import java.util.List;

public class CookingModeActivity extends AppCompatActivity {

    int id;
    RequestManager manager;

    RecyclerView recycler_meal_instructions_cooking_mode;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cooking_mode);

        findViews();
        id = Integer.parseInt(getIntent().getStringExtra("id"));
        manager = new RequestManager(this);
        manager.getInstructions(instructionsListener, id);
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading Details...");
        dialog.show();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void findViews() {
        recycler_meal_instructions_cooking_mode = findViewById(R.id.recycler_meal_instructions_cooking_mode);
    }

    private final InstructionsListener instructionsListener = new InstructionsListener() {
        @Override
        public void didFetch(List<InstructionsResponse> response, String message) {
            dialog.dismiss();

            if (!response.isEmpty() && !response.get(0).steps.isEmpty()) {
                CookingModeAdapter cookingModeAdapter = new CookingModeAdapter(
                        CookingModeActivity.this,
                        response.get(0).steps
                );

                recycler_meal_instructions_cooking_mode.setLayoutManager(
                        new LinearLayoutManager(CookingModeActivity.this, LinearLayoutManager.HORIZONTAL, false)
                );

                recycler_meal_instructions_cooking_mode.setAdapter(cookingModeAdapter);

                LinearSnapHelper snapHelper = new LinearSnapHelper();
                snapHelper.attachToRecyclerView(recycler_meal_instructions_cooking_mode);
            }
        }

        @Override
        public void didError(String message) {
            dialog.dismiss();
        }
    };

}
