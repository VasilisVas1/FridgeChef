package com.example.vassilis88.fridgechef;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vassilis88.fridgechef.Adapters.CookingBookAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CookingBookActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    CookingBookAdapter adapter;
    ArrayList<String> savedRecipeIds = new ArrayList<>();
    FirebaseAuth auth;
    DatabaseReference savedRecipesRef;


    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cooking_book);
        recyclerView = findViewById(R.id.recycler_saved_recipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CookingBookAdapter(this, savedRecipeIds);
        recyclerView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        savedRecipesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("savedRecipes");

        loadSavedRecipes();
    }

    private void refreshData(){
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadSavedRecipes();
        }
    }

    private void loadSavedRecipes() {
        savedRecipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savedRecipeIds.clear();
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    savedRecipeIds.add(recipeSnapshot.getKey());
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
