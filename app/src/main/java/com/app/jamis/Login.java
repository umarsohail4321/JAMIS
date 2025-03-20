package com.app.jamis;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Login extends AppCompatActivity {

    private Button loginButton;
    private EditText emailEditText, passwordEditText;
    private ImageView img;
    private ProgressDialog statusDialog;
    String type = "";
    private FirebaseAuth firebaseAuth; // Firebase Authentication instance

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        if (getIntent().hasExtra("type")){
            type = getIntent().getStringExtra("type");
        }

        emailEditText = findViewById(R.id.edt_email);
        passwordEditText = findViewById(R.id.edt_password);
        loginButton = findViewById(R.id.btn_login);
        img = findViewById(R.id.loginimg);

        setKeyboardVisibilityListener();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (validateInputs(email, password)) {
                    loginUser(email, password);
                }
            }
        });
        emailEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.setVisibility(View.GONE);
            }
        });
        passwordEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.setVisibility(View.GONE);
            }
        });

    }
    private void setKeyboardVisibilityListener() {
        final View activityRootView = findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                        if (heightDiff > 200) { // If more than 200 pixels, it's probably the keyboard.
                            // Check if EditText has focus, and then hide ImageView
                            if (emailEditText.hasFocus() || passwordEditText.hasFocus()) {
                                img.setVisibility(View.GONE);
                            }
                        } else {
                            // If keyboard is hidden, make ImageView visible again
                            img.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            emailEditText.setError("Email is required!");
            emailEditText.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email!");
            emailEditText.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required!");
            passwordEditText.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters!");
            passwordEditText.requestFocus();
            return false;
        }
        return true;
    }

    private void loginUser(String email, String password) {
        statusDialog = new ProgressDialog(Login.this);
        statusDialog.setMessage("Logging in...");
        statusDialog.setIndeterminate(false);
        statusDialog.setCancelable(false);
        statusDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    statusDialog.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Successful login and email verified
                            navigateToDashboard();
                        } else {
                            if(user!=null){
                                user.sendEmailVerification();
                            }
                            Toast.makeText(Login.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Login failed
                        Toast.makeText(Login.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToDashboard() {
        FirebaseDatabase.getInstance().getReference()
                .child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (Objects.requireNonNull(snapshot.child("type").getValue(String.class)).equalsIgnoreCase(type)){
                                if (type.equalsIgnoreCase("Employer")){
                                    Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Login.this, EmployerActivity.class); // Replace with your dashboard activity
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Login.this, EmployeeActivity.class); // Replace with your dashboard activity
                                    startActivity(intent);
                                    finish();
                                }
                            }else{
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(Login.this, "Your account associated with " + Objects.requireNonNull(snapshot.child("type").getValue(String.class)) + " account.", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(Login.this, "Something went wrong with your account please verify by admin.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public void gotosignup(View view) {
        startActivity(new Intent(this, Signup.class)
                .putExtra("type", type));
    }
}
