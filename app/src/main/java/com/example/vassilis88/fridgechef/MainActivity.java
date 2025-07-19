package com.example.vassilis88.fridgechef;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

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
    FirebaseAuth auth;
    DatabaseReference database;
    TextView welcomeTextView;

    ViewPager2 viewPager2;

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

        setupAnimations();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        welcomeTextView = findViewById(R.id.welcome_textview);

        TextView menuDropdown = findViewById(R.id.menu_dropdown);
        menuDropdown.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(MainActivity.this, R.style.PopupMenuStyle);
            PopupMenu popupMenu = new PopupMenu(wrapper, menuDropdown);
            popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.menu_stats) {
                    Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.menu_pantry) {
                    Intent intent = new Intent(MainActivity.this, PantryActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id==R.id.menu_cookbook) {
                    Intent intent = new Intent(MainActivity.this,CookingBookActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
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
                        recipesByIngredientsAdapter.setRecipes(new ArrayList<>());
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
            viewPager2 = findViewById(R.id.viewpager_recipes);

            recipesByIngredientsAdapter = new RecipesByIngredientsAdapter(MainActivity.this, response, recipeClickListener);

            viewPager2.setAdapter(recipesByIngredientsAdapter);
            viewPager2.setPageTransformer((page, position) -> {
                float absPos = Math.abs(position);
                page.setRotation(position * -8f);
                page.setTranslationY(absPos * 15f);

                float scale = Math.max(0.92f, 1 - (0.05f * absPos));

                page.setScaleX(scale);
                page.setScaleY(scale);
                page.setAlpha(Math.max(0.8f, 1 - (0.1f * absPos)));
                page.setTranslationX(position * -20f);
            });
            animateViewPager();
        }
        @Override
        public void didError(String message) {
            dialog.dismiss();
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };

    private void setupAnimations() {
        View welcomeCard = (View) findViewById(R.id.welcome_textview).getParent();
        Animation slideInUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        welcomeCard.startAnimation(slideInUp);

        View toolbar = findViewById(R.id.activity_main);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        toolbar.startAnimation(fadeIn);

        TextView menuButton = findViewById(R.id.menu_dropdown);
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        menuButton.startAnimation(scaleIn);

        animateCookingIcons();
    }

    private void animateCookingIcons() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            LinearLayout welcomeCard = (LinearLayout) findViewById(R.id.welcome_textview).getParent();
            if (welcomeCard instanceof LinearLayout) {
                View iconsLayout = welcomeCard.getChildAt(0);
                if (iconsLayout instanceof LinearLayout) {
                    for (int i = 0; i < ((LinearLayout) iconsLayout).getChildCount(); i++) {
                        View icon = ((LinearLayout) iconsLayout).getChildAt(i);
                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 1.2f, 1f);
                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 1.2f, 1f);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.playTogether(scaleX, scaleY);
                        animatorSet.setDuration(1000);
                        animatorSet.setStartDelay(i * 200);
                        animatorSet.setInterpolator(new DecelerateInterpolator());
                        animatorSet.start();
                    }
                }
            }
        }, 1000);
    }
    private void animateViewPager() {
        if (viewPager2 != null) {
            viewPager2.setAlpha(0f);
            viewPager2.setTranslationY(100f);
            viewPager2.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }
}