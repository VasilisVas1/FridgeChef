package com.example.vassilis88.fridgechef;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.Adapters.PantryAdapter;
import com.example.vassilis88.fridgechef.Listenerts.AutocompleteIngredientsResponseListener;
import com.example.vassilis88.fridgechef.Models.AutocompleteIngredients;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PantryActivity extends AppCompatActivity implements AutocompleteIngredientsResponseListener{

    private FirebaseAuth auth;
    private DatabaseReference database;
    private RecyclerView recyclerView;
    private PantryAdapter pantryAdapter;
    private List<String> ingredientList;
    private EditText quantityInput;
    private AutoCompleteTextView ingredientInput;
    private Spinner unitSpinner;
    private ArrayAdapter<String> unitAdapter;
    private List<AutocompleteIngredients> currentSuggestions;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final String OPENROUTER_API_KEY = BuildConfig.OPENROUTER_API_KEY;
    private AlertDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantry);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        ingredientInput = findViewById(R.id.editText_ingredient);
        unitSpinner = findViewById(R.id.spinner_unit);
        quantityInput = findViewById(R.id.editText_quantity);

        setupIngredientAutocomplete();
        setupUnitSpinner();

        recyclerView = findViewById(R.id.recycler_pantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ingredientList = new ArrayList<>();
        pantryAdapter = new PantryAdapter(PantryActivity.this, ingredientList, new PantryAdapter.OnIngredientClickListener() {
            @Override
            public void onIngredientClick(String ingredient, int position) {
                showDeleteConfirmationDialog(ingredient, position);
            }
        });

        recyclerView.setAdapter(pantryAdapter);

        loadIngredients();

        findViewById(R.id.button_addIngredient).setOnClickListener(v -> addIngredient());
        findViewById(R.id.button_open_camera).setOnClickListener(v -> openCamera());
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            analyzeImageWithOpenRouter(photo);
        }
    }

    private void analyzeImageWithOpenRouter(Bitmap photo) {
        showLoadingDialog();
        new Thread(() -> {
            try {
                String base64Image = bitmapToBase64(photo);

                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "openai/gpt-4o-mini");

                JSONArray messages = new JSONArray();
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", "You are an expert at identifying single food ingredients from images. The user will show you a photo of ONE ingredient. Identify what that single ingredient is and respond with ONLY the ingredient name. No descriptions, no extra text, just the name of the ingredient (e.g., 'Tomato', 'Onion', 'Chicken breast').");
                messages.put(systemMessage);

                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");

                JSONArray content = new JSONArray();

                JSONObject textContent = new JSONObject();
                textContent.put("type", "text");
                textContent.put("text", "What single ingredient is shown in this image? Respond with only the ingredient name.");
                content.put(textContent);

                JSONObject imageContent = new JSONObject();
                imageContent.put("type", "image_url");
                JSONObject imageUrl = new JSONObject();
                imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
                imageContent.put("image_url", imageUrl);
                content.put(imageContent);

                userMessage.put("content", content);
                messages.put(userMessage);

                requestBody.put("messages", messages);
                requestBody.put("max_tokens", 50);
                requestBody.put("temperature", 0.0);

                URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + OPENROUTER_API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(requestBody.toString());
                writer.flush();
                writer.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    runOnUiThread(() -> {
                        hideLoadingDialog();
                        handleOpenRouterResponse(response.toString());
                    });
                } else {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    Log.e("OpenRouter Error", "Response Code: " + responseCode + ", Error: " + errorResponse.toString());
                    runOnUiThread(() -> {
                        hideLoadingDialog();
                        Toast.makeText(this, "Failed to analyze image. Please try again.", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("OpenRouter Error", "Exception: " + e.getMessage());
                runOnUiThread(() -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Error analyzing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String bitmapToBase64(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void handleOpenRouterResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray choices = jsonResponse.optJSONArray("choices");
            if (choices != null && choices.length() > 0) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.optJSONObject("message");

                if (message != null) {
                    String content = message.optString("content", "").trim();

                    if (!content.isEmpty()) {
                        String ingredientName = cleanIngredientName(content);

                        if (!ingredientName.isEmpty()) {
                            ingredientInput.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(ingredientInput, InputMethodManager.SHOW_IMPLICIT);

                            typeWriterEffect(ingredientInput, ingredientName, 100);
                        } else {
                            ingredientInput.setText("No ingredient recognized");
                        }
                    } else {
                        ingredientInput.setText("No ingredient recognized");
                    }
                } else {
                    Log.e("JSON Error", "Message field missing in response.");
                    ingredientInput.setText("Error parsing response");
                }
            } else {
                Log.e("JSON Error", "Choices field missing or empty in response.");
                ingredientInput.setText("No ingredient recognized");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON Error", "Failed to parse JSON: " + e.getMessage());
            ingredientInput.setText("Error parsing response");
        }
    }

    private String cleanIngredientName(String ingredient) {
        ingredient = ingredient.replaceAll("(?i)^(the |a |an |some |fresh |raw |cooked |diced |sliced |chopped |whole |single )", "");
        ingredient = ingredient.replaceAll("(?i)(\\s+\\(.*\\)|\\s*-.*|\\s*,.*|\\s*\\.|\\s*!)", "");
        ingredient = ingredient.replaceAll("(?i)\\s+(is|in|the|image|photo|picture).*", "");
        ingredient = ingredient.trim();
        if (ingredient.toLowerCase().endsWith("es") && ingredient.length() > 3) {
            if (ingredient.toLowerCase().endsWith("oes")) {
                ingredient = ingredient.substring(0, ingredient.length() - 2);
            } else if (ingredient.toLowerCase().endsWith("ies")) {
                ingredient = ingredient.substring(0, ingredient.length() - 3) + "y";
            }
        } else if (ingredient.toLowerCase().endsWith("s") && ingredient.length() > 3 &&
                !ingredient.toLowerCase().endsWith("ss")) {
            ingredient = ingredient.substring(0, ingredient.length() - 1);
        }
        if (!ingredient.isEmpty()) {
            ingredient = ingredient.substring(0, 1).toUpperCase() + ingredient.substring(1).toLowerCase();
        }

        return ingredient;
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        builder.setView(view);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void typeWriterEffect(final AutoCompleteTextView inputView, final String text, final long delay) {
        final Handler handler = new Handler();
        inputView.setText("");

        for (int i = 0; i <= text.length(); i++) {
            final int index = i;
            handler.postDelayed(() -> {
                inputView.setText(text.substring(0, index));
                inputView.setSelection(index); // Keeps cursor at the end
            }, delay * i);
        }
    }

    private void setupIngredientAutocomplete() {
        ingredientInput.setThreshold(1);
        ingredientInput.setOnItemClickListener((parent, view, position, id) -> {
            AutocompleteIngredients selected = currentSuggestions.get(position);
            updateUnitSpinner(selected.possibleUnits);
        });

        ingredientInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0) {
                    new RequestManager(PantryActivity.this)
                            .getAutoCompleteIngredients(PantryActivity.this, s.toString());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupUnitSpinner() {
        unitAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, new ArrayList<>());
        unitAdapter.setDropDownViewResource(R.layout.spinner_item);
        unitSpinner.setAdapter(unitAdapter);
    }

    private void updateUnitSpinner(List<String> units) {
        unitAdapter.clear();
        unitAdapter.addAll(units);
        unitAdapter.notifyDataSetChanged();
    }

    @Override
    public void didFetch(List<AutocompleteIngredients> response, String message) {
        currentSuggestions = response;
        List<String> suggestions = new ArrayList<>();
        for (AutocompleteIngredients ingredient : response) {
            suggestions.add(ingredient.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, suggestions);
        ingredientInput.setAdapter(adapter);
    }

    @Override
    public void didError(String message) {
        Toast.makeText(this, "Autocomplete error: " + message, Toast.LENGTH_SHORT).show();
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
                                String quantity = ingredientData.get("quantity");
                                String unit = ingredientData.get("unit");

                                if (unit == null) {
                                    unit = "";
                                }
                                ingredientList.add(ingredient + " - " + quantity + " " + unit);
                            }
                        }
                        pantryAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(PantryActivity.this, "Failed to load ingredients.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteConfirmationDialog(String selectedItem, int position) {
        String ingredientName = selectedItem.split(" - ")[0].trim();

        new androidx.appcompat.app.AlertDialog.Builder(PantryActivity.this)
                .setTitle("Remove Ingredient")
                .setMessage("Do you want to remove '" + ingredientName + "' from your pantry?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteIngredientFromDatabase(selectedItem, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteIngredientFromDatabase(String selectedItem, int position) {
        String userId = auth.getCurrentUser().getUid();
        String ingredientName = selectedItem.split(" - ")[0].trim();

        database.child("users").child(userId).child("pantry")
                .orderByChild("name").equalTo(ingredientName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().removeValue();
                        }
                        ingredientList.remove(selectedItem);
                        pantryAdapter.notifyDataSetChanged();
                        Toast.makeText(PantryActivity.this, "Ingredient removed.", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(PantryActivity.this, "Failed to remove ingredient.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addIngredient() {
        String ingredientName = ingredientInput.getText().toString().trim();
        String quantity = quantityInput.getText().toString().trim();
        if (unitSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a unit.", Toast.LENGTH_SHORT).show();
            return;
        }
        String unit = unitSpinner.getSelectedItem().toString();
        if (ingredientName.isEmpty() || quantity.isEmpty() || unit.isEmpty()) {
            Toast.makeText(this, "Please enter both ingredient and quantity.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = auth.getCurrentUser().getUid();
        database.child("users").child(userId).child("pantry")
                .orderByChild("name").equalTo(ingredientName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String existingKey = dataSnapshot.getChildren().iterator().next().getKey();
                            showReplaceIngredientDialog(ingredientName, quantity, unit, existingKey);
                        } else {
                            Map<String, String> ingredientData = Map.of(
                                    "name", ingredientName,
                                    "quantity", quantity,
                                    "unit", unit
                            );
                            database.child("users").child(userId).child("pantry").push().setValue(ingredientData)
                                    .addOnSuccessListener(aVoid -> {
                                        ingredientInput.setText("");
                                        quantityInput.setText("");
                                        unitAdapter.clear();
                                        unitAdapter.notifyDataSetChanged();
                                        loadIngredients();
                                        Toast.makeText(PantryActivity.this, "Ingredient added!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(PantryActivity.this, "Failed to add ingredient.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(PantryActivity.this, "Failed to check existing ingredients.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void showReplaceIngredientDialog(String ingredientName, String quantity, String unit, String existingKey) {
        new androidx.appcompat.app.AlertDialog.Builder(PantryActivity.this)
                .setTitle("Ingredient Already Exists")
                .setMessage("'" + ingredientName + "' is already in your pantry. Do you want to replace it with the new quantity?")
                .setPositiveButton("Replace", (dialog, which) -> {
                    String userId = auth.getCurrentUser().getUid();
                    Map<String, String> ingredientData = Map.of(
                            "name", ingredientName,
                            "quantity", quantity,
                            "unit", unit
                    );
                    database.child("users").child(userId).child("pantry").child(existingKey).setValue(ingredientData)
                            .addOnSuccessListener(aVoid -> {
                                ingredientInput.setText("");
                                quantityInput.setText("");
                                unitAdapter.clear();
                                unitAdapter.notifyDataSetChanged();
                                loadIngredients();
                                Toast.makeText(PantryActivity.this, "Ingredient updated!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(PantryActivity.this, "Failed to update ingredient.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}