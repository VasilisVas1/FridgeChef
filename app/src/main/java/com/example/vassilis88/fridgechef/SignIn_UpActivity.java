package com.example.vassilis88.fridgechef;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignIn_UpActivity extends AppCompatActivity {

    EditText input_full_name,input_email,input_password;
    TextView textView_signIn,textviewTitle_sign_page;
    Button button_submit_sign_up;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference database;

    private View floatingCircle1, floatingCircle2, floatingCircle3, floatingCircle4, floatingCircle5;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        input_full_name=findViewById(R.id.input_full_name);
        input_email=findViewById(R.id.input_email);
        input_password=findViewById(R.id.input_password);
        textView_signIn=findViewById(R.id.textView_signIn);
        textviewTitle_sign_page=findViewById(R.id.textviewTitle_sign_page);
        button_submit_sign_up=findViewById(R.id.button_submit_sign_up);
        auth=FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        initFloatingCircles();
        startFloatingAnimations();
        textView_signIn.setOnClickListener(this::changeToSignIn);
        button_submit_sign_up.setOnClickListener(this::onSignUpClick);
    }

    private void changeToSignIn(View view){
        textviewTitle_sign_page.setText("Sign in to your FridgeChef account");
        textView_signIn.setText("Don't have an account? Sign Up");
        textView_signIn.setOnClickListener(this::changeToSignUp);
        button_submit_sign_up.setText("Sign in");
        button_submit_sign_up.setOnClickListener(this::onSignInClick);
        input_full_name.setVisibility(View.GONE);
    }

    private void changeToSignUp(View view){
        textviewTitle_sign_page.setText("Create your FridgeChef account");
        textView_signIn.setText("Already have an account? Sign In");
        textView_signIn.setOnClickListener(this::changeToSignIn);
        button_submit_sign_up.setText("Sign up");
        button_submit_sign_up.setOnClickListener(this::onSignUpClick);
        input_full_name.setVisibility(View.VISIBLE);
    }

    public void onSignInClick(View view) {
        if (!input_email.getText().toString().isEmpty() && !input_password.getText().toString().isEmpty()) {
            String email = input_email.getText().toString();
            String password = input_password.getText().toString();
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = auth.getCurrentUser();
                                if (user != null) {
                                    Intent intent = new Intent(SignIn_UpActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                showMessage("Error", task.getException().getLocalizedMessage());
                            }
                        }
                    });
        } else {
            showMessage("Error", "Please enter both email and password");
        }
    }

    private void onSignUpClick(View view){
        if (!input_email.getText().toString().isEmpty()
                && !input_password.getText().toString().isEmpty() && !input_full_name.getText().toString().isEmpty()) {
            String email = input_email.getText().toString();
            String password = input_password.getText().toString();
            String full_name = input_full_name.getText().toString();
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                user = auth.getCurrentUser();
                                if (user != null) {
                                    String userId = user.getUid();
                                    database.child("users").child(userId).child("displayName").setValue(full_name);
                                    Intent intent = new Intent(SignIn_UpActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                showMessage("Error", task.getException().getLocalizedMessage());
                            }
                        }
                    });
        } else {
            showMessage("Error", "Please provide data to the fields");
        }
    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }


    private void initFloatingCircles() {
        floatingCircle1 = findViewById(R.id.floating_circle_1);
        floatingCircle2 = findViewById(R.id.floating_circle_2);
        floatingCircle3 = findViewById(R.id.floating_circle_3);
        floatingCircle4 = findViewById(R.id.floating_circle_4);
        floatingCircle5 = findViewById(R.id.floating_circle_5);
    }

    private void startFloatingAnimations() {
        new Handler().postDelayed(() -> startAnimation(floatingCircle1, R.anim.float_up_down), 100);
        new Handler().postDelayed(() -> startAnimation(floatingCircle2, R.anim.float_rotation), 500);
        new Handler().postDelayed(() -> startAnimation(floatingCircle3, R.anim.float_side_to_side), 1000);
        new Handler().postDelayed(() -> startAnimation(floatingCircle4, R.anim.float_pulse), 1500);
        new Handler().postDelayed(() -> startAnimation(floatingCircle5, R.anim.float_diagonal), 2000);
        startContinuousFloating();
    }

    private void startAnimation(View view, int animationResource) {
        Animation animation = AnimationUtils.loadAnimation(this, animationResource);
        view.startAnimation(animation);
    }

    private void startContinuousFloating() {
        createFloatingAnimator(floatingCircle1, 0, -15, 3000);
        createFloatingAnimator(floatingCircle2, 0, 10, 4000);
        createFloatingAnimator(floatingCircle3, -10, 0, 3500);
        createFloatingAnimator(floatingCircle4, 0, -8, 2500);
        createFloatingAnimator(floatingCircle5, 8, -12, 4500);
    }

    private void createFloatingAnimator(View view, float startX, float startY, long duration) {
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(view, "translationY", startY, startY + 20);
        animatorY.setDuration(duration);
        animatorY.setRepeatCount(ObjectAnimator.INFINITE);
        animatorY.setRepeatMode(ObjectAnimator.REVERSE);
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(view, "translationX", startX, startX + 15);
        animatorX.setDuration(duration + 1000);
        animatorX.setRepeatCount(ObjectAnimator.INFINITE);
        animatorX.setRepeatMode(ObjectAnimator.REVERSE);
        ObjectAnimator animatorRotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        animatorRotation.setDuration(duration * 2);
        animatorRotation.setRepeatCount(ObjectAnimator.INFINITE);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, "alpha", 0.1f, 0.3f);
        animatorAlpha.setDuration(duration / 2);
        animatorAlpha.setRepeatCount(ObjectAnimator.INFINITE);
        animatorAlpha.setRepeatMode(ObjectAnimator.REVERSE);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorY, animatorX, animatorRotation, animatorAlpha);
        animatorSet.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopFloatingAnimations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startFloatingAnimations();
    }

    private void stopFloatingAnimations() {
        if (floatingCircle1 != null) floatingCircle1.clearAnimation();
        if (floatingCircle2 != null) floatingCircle2.clearAnimation();
        if (floatingCircle3 != null) floatingCircle3.clearAnimation();
        if (floatingCircle4 != null) floatingCircle4.clearAnimation();
        if (floatingCircle5 != null) floatingCircle5.clearAnimation();
    }
}