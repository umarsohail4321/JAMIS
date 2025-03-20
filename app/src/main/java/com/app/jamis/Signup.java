package com.app.jamis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Signup extends AppCompatActivity {

    private Button btnSignup;
    private EditText editFullName, editEmail, editPassword;
    private ImageView img;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth; // Firebase Authentication instance
    private DatabaseReference databaseReference; // Firebase Realtime Database reference
    String type = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        btnSignup = findViewById(R.id.btnsignup);
        editFullName = findViewById(R.id.edtfullname);
        editEmail = findViewById(R.id.edtemailaddress);
        editPassword = findViewById(R.id.edtpassword);
        progressBar = findViewById(R.id.progressbar);
        img = findViewById(R.id.signinImg);
        setKeyboardVisibilityListener();

        type = getIntent().getStringExtra("type");
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = editFullName.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                // Validate inputs
                if (validateInputs(fullName, email, password)) {
                    // Register user if inputs are valid
                    registerUser(fullName, email, password);
                }
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
                            if (editFullName.hasFocus() || editEmail.hasFocus() || editPassword.hasFocus()) {
                                img.setVisibility(View.GONE);
                            }
                        } else {
                            // If keyboard is hidden, make ImageView visible again
                            img.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }


    private boolean validateInputs(String fullName, String email, String password) {
        if (fullName.isEmpty()) {
            editFullName.setError("Full Name is Required!");
            editFullName.requestFocus();
            return false;
        }
        if (!fullName.matches("^[A-Za-z ]+$")) {
            editFullName.setError("Full Name should contain only Alphabet");
            editFullName.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            editEmail.setError("Email is Required!");
            editEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Please provide a valid email!");
            editEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            editPassword.setError("Password is Required!");
            editPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            editPassword.setError("Password must be at least 6 characters!");
            editPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void registerUser(String fullName, String email, String password) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Save user to Firebase Realtime Database
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        firebaseUser.sendEmailVerification();
                        if (firebaseUser != null) {
                            User user = new User(fullName, email);
                            user.setType(type);
                            databaseReference.child(firebaseUser.getUid())
                                    .setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(Signup.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                                            navigateToLogin();
                                        } else {
                                            Toast.makeText(Signup.this, "Failed to save user: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(Signup.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(Signup.this, Login.class);
        intent.putExtra("type", type);
        startActivity(intent);
        finish();
    }

    public void gotologin(View view) {
        navigateToLogin();
    }
}
