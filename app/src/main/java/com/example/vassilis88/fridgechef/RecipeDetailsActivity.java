package com.example.vassilis88.fridgechef;

import android.app.ProgressDialog;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vassilis88.fridgechef.Adapters.IngredientsAdapter;
import com.example.vassilis88.fridgechef.Adapters.InstructionsAdapter;
import com.example.vassilis88.fridgechef.Listenerts.InstructionsListener;
import com.example.vassilis88.fridgechef.Listenerts.RecipeDetailsListener;
import com.example.vassilis88.fridgechef.Models.InstructionsResponse;
import com.example.vassilis88.fridgechef.Models.RecipeDetailsResponse;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import android.util.Base64;
import java.nio.file.Files;


public class RecipeDetailsActivity extends AppCompatActivity {
    int id;
    String stringId;
    TextView textView_meal_name;
    ImageView imageView_meal_image;

    RecyclerView recycler_meal_ingredients,recycler_meal_instructions;

    RequestManager manager;
    ProgressDialog dialog;

    IngredientsAdapter ingredientsAdapter;
    InstructionsAdapter instructionsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_details);

        findViews();
        stringId=getIntent().getStringExtra("id");
        id =Integer.parseInt(getIntent().getStringExtra("id"));

        manager=new RequestManager(this);
        manager.getRecipeDetails(recipeDetailsListener,id);
        manager.getInstructions(instructionsListener,id);
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading Details...");

        Button shareButton = findViewById(R.id.button_share_recipe);
        shareButton.setOnClickListener(v -> generateAndUploadRecipe());

        dialog.show();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button cookingModeButton = findViewById(R.id.button_cooking_mode);

        cookingModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecipeDetailsActivity.this, CookingModeActivity.class)
                        .putExtra("id",stringId));
            }
        });
    }

    private void findViews() {
        textView_meal_name=findViewById(R.id.textView_meal_name);
        imageView_meal_image=findViewById(R.id.imageView_meal_image);
        recycler_meal_ingredients=findViewById(R.id.recycler_meal_ingredients);
        recycler_meal_instructions=findViewById(R.id.recycler_meal_instructions);
    }

    private final RecipeDetailsListener recipeDetailsListener= new RecipeDetailsListener() {
        @Override
        public void didFetch(RecipeDetailsResponse response, String message) {
            dialog.dismiss();
            textView_meal_name.setText(response.title);
            Picasso.get().load(response.image).into(imageView_meal_image);

            recycler_meal_ingredients.setHasFixedSize(true);
            recycler_meal_ingredients.setLayoutManager(new LinearLayoutManager(RecipeDetailsActivity.this,LinearLayoutManager.VERTICAL,false));
            ingredientsAdapter = new IngredientsAdapter(RecipeDetailsActivity.this, response.extendedIngredients);
            recycler_meal_ingredients.setAdapter(ingredientsAdapter);

        }

        @Override
        public void didError(String message) {
            Toast.makeText(RecipeDetailsActivity.this, message, Toast.LENGTH_SHORT).show();

        }
    };


    private final InstructionsListener instructionsListener = new InstructionsListener() {
        @Override
        public void didFetch(List<InstructionsResponse> response, String message) {
            dialog.dismiss();
            recycler_meal_instructions.setHasFixedSize(true);
            recycler_meal_instructions.setLayoutManager(new LinearLayoutManager(RecipeDetailsActivity.this, LinearLayoutManager.VERTICAL,false));

            instructionsAdapter = new InstructionsAdapter(RecipeDetailsActivity.this, response);
            recycler_meal_instructions.setAdapter(instructionsAdapter);
        }
        @Override
        public void didError(String messsage) {

        }
    };

    private void generateAndUploadRecipe() {
        TextView recipeNameView=findViewById(R.id.textView_meal_name);
        String recipeTitle2 = recipeNameView.getText().toString();

        ImageView recipeImageView = findViewById(R.id.imageView_meal_image);
        Bitmap recipeBitmap = ((BitmapDrawable) recipeImageView.getDrawable()).getBitmap();
        String encodedImage = encodeImageToBase64(recipeBitmap);

        String fileName = "recipe_" + System.currentTimeMillis() + ".html";


        String htmlContent = "<html><head><meta charset='UTF-8'>"
                + "<title>" + recipeTitle2 + "</title>"
                + "<style> body { font-family: Arial, sans-serif; padding: 20px; background: #FFF3E0; font-size: 18px; }"
                + " h1 { color: #8B4513; text-align: center; font-size: 30px; }"
                + " .container { background: white; padding: 15px; border-radius: 8px; box-shadow: 2px 2px 10px rgba(0,0,0,0.1); }"
                + " .recipe-image { width: 100%; max-height: 250px; object-fit: cover; border-radius: 8px; }"
                + " .section { margin-top: 20px; }"
                + " .section h2 { color: #8B4513; border-bottom: 2px solid #8B4513; padding-bottom: 5px; font-size: 24px; }"
                + " ul { padding-left: 20px; font-size: 18px; }"
                + "</style></head><body>"
                + "<div class='container'>"
                + "<h1>" + recipeTitle2 + "</h1>"
                + "<img class='recipe-image' src='data:image/png;base64," + encodedImage + "'/>"
                + "<div class='section'><h2>Ingredients</h2>" + ingredientsAdapter.getIngredientsAsHtml() + "</div>"
                + "<div class='section'><h2>Instructions</h2>" + instructionsAdapter.getInstructionsAsHtml()+ "</div>"
                + "</div></body></html>";



        File htmlFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        try {
            FileOutputStream fos = new FileOutputStream(htmlFile);
            fos.write(htmlContent.toString().getBytes(StandardCharsets.UTF_8));
            fos.close();

            uploadHtmlToGitHub(htmlFile, fileName);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create HTML file", Toast.LENGTH_SHORT).show();
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }



    private void uploadHtmlToGitHub(File htmlFile, String fileName) {
        String githubRepo = "VasilisVas1/recipe-sharing";
        String githubToken = BuildConfig.GITHUB_TOKEN;
        String githubBranch = "main";

        ProgressDialog progressDialog = new ProgressDialog(RecipeDetailsActivity.this);
        progressDialog.setMessage("Uploading... Please wait");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            byte[] fileBytes = Files.readAllBytes(htmlFile.toPath());
            String encodedFile = Base64.encodeToString(fileBytes, Base64.NO_WRAP);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("message", "Upload recipe: " + fileName);
            jsonBody.put("content", encodedFile);
            jsonBody.put("branch", githubBranch);

            String apiUrl = "https://api.github.com/repos/" + githubRepo + "/contents/" + fileName;

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), jsonBody.toString());

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "token " + githubToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .put(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(RecipeDetailsActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.e("GitHub Response", responseBody); // Log the response for debugging

                    if (response.isSuccessful()) {
                        String githubUrl = "https://VasilisVas1.github.io/recipe-sharing/" + fileName;

                        runOnUiThread(() -> {
                            progressDialog.setMessage("Upload successful! Preparing link...");

                            new Handler().postDelayed(() -> {
                                progressDialog.dismiss();

                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this delicious recipe! The link will be live in just a few seconds—hang tight!" + githubUrl);
                                startActivity(Intent.createChooser(shareIntent, "Share Recipe Link"));

                            }, 20000); // 10-second delay

                        });

                    } else {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(RecipeDetailsActivity.this, "Upload Failed: " + responseBody, Toast.LENGTH_SHORT).show();
                        });
                    }
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}