package com.example.vassilis88.fridgechef;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.Adapters.StatisticsAdapter;
import com.example.vassilis88.fridgechef.Adapters.StatisticsUserIngredientsAdapter;
import com.example.vassilis88.fridgechef.Models.Ingredient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private RecyclerView recyclerView,recyclerYourIngredients;
    private TextView conclusionTextView;
    private StatisticsAdapter adapter;
    private StatisticsUserIngredientsAdapter userIngredientsAdapter;
    private List<Ingredient> topIngredients = new ArrayList<>();
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private List<String> ingredientList;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ingredientList = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        recyclerView = findViewById(R.id.recycler_stats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StatisticsAdapter(topIngredients);
        recyclerView.setAdapter(adapter);

        conclusionTextView = findViewById(R.id.conclusionTextView);
        recyclerYourIngredients = findViewById(R.id.recycler_youringredients);
        recyclerYourIngredients.setLayoutManager(new LinearLayoutManager(this));
        userIngredientsAdapter = new StatisticsUserIngredientsAdapter(ingredientList);
        recyclerYourIngredients.setAdapter(userIngredientsAdapter);


        dbRef = FirebaseDatabase.getInstance().getReference("users");
        loadIngredients();
        fetchIngredientData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_stats), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchIngredientData() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("StatsActivity", "Data Snapshot: " + snapshot.getValue());
                Map<String, Integer> ingredientCount = new HashMap<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    DataSnapshot pantrySnapshot = userSnapshot.child("pantry");
                    for (DataSnapshot item : pantrySnapshot.getChildren()) {
                        String name = item.child("name").getValue(String.class);
                        if (name != null) {
                            name = name.toLowerCase();
                            ingredientCount.put(name, ingredientCount.getOrDefault(name, 0) + 1);
                        }
                    }
                }
                List<Map.Entry<String, Integer>> sorted = new ArrayList<>(ingredientCount.entrySet());
                sorted.sort((a, b) -> b.getValue() - a.getValue());
                topIngredients.clear();
                for (int i = 0; i < Math.min(10, sorted.size()); i++) {
                    Map.Entry<String, Integer> entry = sorted.get(i);
                    topIngredients.add(new Ingredient(entry.getKey(), entry.getValue()));
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StatsActivity", "Firebase error: " + error.getMessage());
            }
        });
    }


    private void loadIngredients() {
        String userId = auth.getCurrentUser().getUid();
        database.child("users").child(userId).child("pantry")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ingredientList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Map<String, String> ingredientData = (Map<String, String>) snapshot.getValue();
                            if (ingredientData != null) {
                                String ingredient = ingredientData.get("name");

                                ingredientList.add(ingredient);
                            }
                        }
                        userIngredientsAdapter.notifyDataSetChanged();
                        analyzePantryHealth();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(StatisticsActivity.this, "Failed to load ingredients.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private String getApiKey() {
        return BuildConfig.SPOONACULAR_API_KEY;
    }

    private void analyzePantryHealth() {
        new Thread(() -> {
            int totalFinalScore = 0;
            int count = 0;

            List<String> ingredientRatings = new ArrayList<>();

            for (String ingredient : ingredientList) {
                try {
                    String searchUrl = "https://api.spoonacular.com/food/ingredients/search?query=" +
                            URLEncoder.encode(ingredient, "UTF-8") +
                            "&number=1&apiKey=" + getApiKey();
                    URL url1 = new URL(searchUrl);
                    HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();

                    conn1.setRequestMethod("GET");
                    BufferedReader reader1 = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
                    StringBuilder result1 = new StringBuilder();
                    String line;
                    while ((line = reader1.readLine()) != null) result1.append(line);
                    reader1.close();

                    JSONObject searchResult = new JSONObject(result1.toString());
                    JSONArray searchItems = searchResult.getJSONArray("results");

                    if (searchItems.length() == 0) continue;

                    JSONObject ingredientJson = searchItems.getJSONObject(0);
                    if (!ingredientJson.has("id")) continue;
                    int id = ingredientJson.getInt("id");

                    String infoUrl = "https://api.spoonacular.com/food/ingredients/" + id +
                            "/information?amount=100&unit=grams&apiKey=" + getApiKey();
                    URL url2 = new URL(infoUrl);
                    HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                    conn2.setRequestMethod("GET");
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                    StringBuilder result2 = new StringBuilder();
                    while ((line = reader2.readLine()) != null) result2.append(line);
                    reader2.close();
                    JSONObject nutritionResult = new JSONObject(result2.toString());
                    JSONArray nutrients = nutritionResult.getJSONObject("nutrition").getJSONArray("nutrients");

                    double energy = 0, saturatedFat = 0, sugars = 0, sodium = 0;
                    double fiber = 0, protein = 0;
                    double fvlPercent = 0;
                    for (int i = 0; i < nutrients.length(); i++) {
                        JSONObject nutrient = nutrients.getJSONObject(i);
                        String name = nutrient.getString("name");
                        double amount = nutrient.getDouble("amount");
                        switch (name) {
                            case "Calories": energy = amount * 4.184; break; // Convert kcal to kJ
                            case "Saturated Fat": saturatedFat = amount; break;
                            case "Sugar": sugars = amount; break;
                            case "Sodium": sodium = amount; break;
                            case "Fiber": fiber = amount; break;
                            case "Protein": protein = amount; break;
                        }
                    }

                    int neg = 0;
                    neg += (energy > 3350) ? 10 : (int) (energy / 335);
                    neg += (saturatedFat > 10) ? 10 : (int) (saturatedFat);
                    neg += (sugars > 45) ? 10 : (int) (sugars / 4.5);
                    neg += (sodium > 900) ? 10 : (int) (sodium / 90);
                    int pos = 0;
                    pos += (fiber >= 4.7) ? 5 : (int) (fiber);
                    pos += (protein >= 8) ? 5 : (int) (protein / 1.6);
                    pos += (fvlPercent >= 80) ? 5 : (fvlPercent >= 60) ? 2 : (fvlPercent >= 40) ? 1 : 0;
                    int finalScore = neg - pos;
                    String rating;
                    if (finalScore <= -1) rating = "A (Excellent)";
                    else if (finalScore <= 2) rating = "B (Good)";
                    else if (finalScore <= 10) rating = "C (Moderate)";
                    else if (finalScore <= 18) rating = "D (Unhealthy)";
                    else rating = "E (Very Unhealthy)";
                    ingredientRatings.add(ingredient + ": " + rating);
                    totalFinalScore += finalScore;
                    count++;
                } catch (Exception e) {
                    Log.e("PantryHealth", "Error with ingredient: " + ingredient, e);
                }
            }
            double avgScore = count > 0 ? (double) totalFinalScore / count : 0;
            Log.d("PantryHealth", "Total Nutri-Score: " + totalFinalScore + ", Count: " + count + ", Avg Score: " + avgScore);
            runOnUiThread(() -> {
                StringBuilder detailedResult = new StringBuilder();
                for (String line : ingredientRatings) {
                    detailedResult.append(line).append("\n");
                }
                String message;
                if (ingredientList.size()==0){
                    message="Your pantry is empty";
                    conclusionTextView.setText(
                            message
                    );
                }else{
                    if (avgScore <= 2) message = "🌿 Your pantry is very healthy!\n";
                    else if (avgScore <= 10) message = "🍎 Your pantry is moderately healthy.\n";
                    else message = "⚠️ Your pantry could be healthier. Try adding more fresh produce and whole foods.\n";
                    conclusionTextView.setText(
                            message + "Ingredient Ratings:\n" + detailedResult.toString()
                    );
                }

            });
        }).start();
    }
}