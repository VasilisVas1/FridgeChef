package com.example.vassilis88.fridgechef;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.Adapters.RecipesByIngredientsAdapter;
import com.example.vassilis88.fridgechef.Listenerts.RecipeByIngredientsResponseListener;
import com.example.vassilis88.fridgechef.Listenerts.RecipeClickListener;
import com.example.vassilis88.fridgechef.Models.RecipesByIngredientsResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ProgressDialog dialog;
    RequestManager manager;
    RecipesByIngredientsAdapter recipesByIngredientsAdapter;
    RecyclerView recyclerView;
    FirebaseAuth auth;
    DatabaseReference database;
    TextView welcomeTextView,pantry_textview;

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        welcomeTextView = findViewById(R.id.welcome_textview);
        pantry_textview = findViewById(R.id.pantry_textview);
        pantry_textview.setOnClickListener(this::openPantry);


        dialog=new ProgressDialog(this);
        dialog.setTitle("Loading...");
        dialog.show();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void refreshData(){
        dialog.show();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            fetchUserIngredients(userId);

            database.child("users").child(userId).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String fullName = dataSnapshot.getValue(String.class);
                    if (fullName != null) {
                        welcomeTextView.setText("Welcome " + fullName + "! Are you ready to cook?");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void fetchUserIngredients(String userId) {
        database.child("users").child(userId).child("pantry").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> ingredientsList = new ArrayList<>();

                for (DataSnapshot ingredientSnapshot : dataSnapshot.getChildren()) {
                    String name = ingredientSnapshot.child("name").getValue(String.class);
                    String quantity = ingredientSnapshot.child("quantity").getValue(String.class);
                    String unit = ingredientSnapshot.child("unit").getValue(String.class);

                    if (name != null && quantity != null && unit != null) {
                        ingredientsList.add(name + " " + quantity + " " + unit);
                    }
                }

                if (!ingredientsList.isEmpty()) {
                    String formattedIngredients = String.join(",", ingredientsList);
                    fetchRecipes(formattedIngredients);
                } else {
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, "Your pantry is empty. Add ingredients!", Toast.LENGTH_SHORT).show();

                    if (recipesByIngredientsAdapter != null) {
                        recipesByIngredientsAdapter.setRecipes(new ArrayList<>()); // clear data
                        recipesByIngredientsAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Failed to load ingredients.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecipes(String ingredients) {
        manager = new RequestManager(this);
        manager.getRecipesByIngredients(recipeByIngredientsResponseListener, ingredients);
    }

    private void openPantry(View view){
        Intent intent = new Intent(MainActivity.this, PantryActivity.class);
        startActivity(intent);
    }


    private final RecipeClickListener recipeClickListener = new RecipeClickListener() {
        @Override
        public void onRecipeClicked(String id) {
            startActivity(new Intent(MainActivity.this, RecipeDetailsActivity.class)
                    .putExtra("id", id));
        }
    };

    private final RecipeByIngredientsResponseListener recipeByIngredientsResponseListener = new RecipeByIngredientsResponseListener() {
        @Override
        public void didFetch(List<RecipesByIngredientsResponse> response, String message) {
            dialog.dismiss();
            recyclerView = findViewById(R.id.recycler_recipeByIngredients);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 1));
            recipesByIngredientsAdapter=new RecipesByIngredientsAdapter(MainActivity.this,response,recipeClickListener);
            recyclerView.setAdapter(recipesByIngredientsAdapter);
        }

        @Override
        public void didError(String message) {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

        }
    };
}