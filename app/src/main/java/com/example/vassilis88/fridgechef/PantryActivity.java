package com.example.vassilis88.fridgechef;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
import java.io.DataOutputStream;
import java.io.InputStreamReader;
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
    private static final String LOGMEAL_API_KEY = BuildConfig.LOGMEAL_API_KEY;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            Bitmap resizedPhoto = resizeAndConvertToGrayscale(photo);
            uploadImageToLogMeal(resizedPhoto);
        }
    }

    private Bitmap resizeAndConvertToGrayscale(Bitmap original) {
        Bitmap resized = Bitmap.createScaledBitmap(original, 512, 512, true);

        Bitmap grayscale = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < 512; x++) {
            for (int y = 0; y < 512; y++) {
                int pixel = resized.getPixel(x, y);
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                int gray = (int) (0.3 * red + 0.59 * green + 0.11 * blue);
                int grayPixel = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;

                grayscale.setPixel(x, y, grayPixel);
            }
        }
        return grayscale;
    }



    private void uploadImageToLogMeal(Bitmap photo) {
        showLoadingDialog();
        new Thread(() -> {
            try {
                String url = "https://api.logmeal.com/v2/image/segmentation/complete";
                URL serverUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Bearer " + LOGMEAL_API_KEY);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary");

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                byte[] imageBytes = outputStream.toByteArray();

                DataOutputStream requestBody = new DataOutputStream(connection.getOutputStream());
                requestBody.writeBytes("------WebKitFormBoundary\r\n");
                requestBody.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n");
                requestBody.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                requestBody.write(imageBytes);
                requestBody.writeBytes("\r\n------WebKitFormBoundary--\r\n");
                requestBody.flush();
                requestBody.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    runOnUiThread(() -> handleApiResponse(response.toString()));
                } else {
                    Log.e("API Error", "Response Code: " + responseCode);
                }

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
                        handleApiResponse(response.toString());
                    });
                } else {
                    Log.e("API Error", "Response Code: " + responseCode);
                    runOnUiThread(this::hideLoadingDialog);
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(this::hideLoadingDialog);

            }
        }).start();
    }

    private AlertDialog loadingDialog;

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


    private void handleApiResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray segmentationResults = jsonResponse.optJSONArray("segmentation_results");

            if (segmentationResults != null && segmentationResults.length() > 0) {
                JSONObject firstSegment = segmentationResults.getJSONObject(0);
                JSONArray recognitionResults = firstSegment.optJSONArray("recognition_results");

                if (recognitionResults != null && recognitionResults.length() > 0) {
                    String bestIngredient = null;
                    double maxProb = 0.0;

                    // Find ingredient with the highest probability
                    for (int i = 0; i < recognitionResults.length(); i++) {
                        JSONObject ingredient = recognitionResults.getJSONObject(i);
                        double prob = ingredient.optDouble("prob", 0.0);
                        if (prob > maxProb) {
                            maxProb = prob;
                            bestIngredient = ingredient.optString("name");
                        }
                    }

                    if (bestIngredient != null) {
                        ingredientInput.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(ingredientInput, InputMethodManager.SHOW_IMPLICIT);

                        typeWriterEffect(ingredientInput, bestIngredient, 300);
                        //ingredientInput.setText(bestIngredient);

                    } else {
                        ingredientInput.setText("No ingredient recognized");
                    }
                } else {
                    Log.e("JSON Error", "'recognition_results' field missing or empty.");
                    ingredientInput.setText("No ingredient recognized");
                }
            } else {
                Log.e("JSON Error", "'segmentation_results' field missing or empty.");
                ingredientInput.setText("No ingredient recognized");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON Error", "Failed to parse JSON: " + e.getMessage());
            ingredientInput.setText("Error parsing response");
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
                                String unit = ingredientData.get("unit"); // Retrieve unit

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
        new androidx.appcompat.app.AlertDialog.Builder(PantryActivity.this)
                .setTitle("Remove Ingredient")
                .setMessage("Do you want to remove this ingredient from your pantry?")
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
                        pantryAdapter.notifyDataSetChanged(); // Full refresh is safer in this case
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