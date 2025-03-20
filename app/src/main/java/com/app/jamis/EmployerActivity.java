package com.app.jamis;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.app.jamis.databinding.EmployerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class EmployerActivity extends AppCompatActivity {
    EmployerBinding binding;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EmployerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(Color.parseColor("#FF000000"));
         databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) {
            // Generate the FCM token
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    // Handle failure
                    Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get the generated FCM token
                String fcmToken = task.getResult();

                // Save the token to Firebase under the user's data
                saveFcmTokenToDatabase(userId, fcmToken);
            });
        }



        FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (!snapshot.child("company").exists() ||
                                    snapshot.child("company").getValue(String.class).isEmpty()){
                                Toast.makeText(EmployerActivity.this, "Complete your profile first.", Toast.LENGTH_SHORT).show();
                                replaceFragment(new employerprofFragment());
                            }else{
                                replaceFragment(new employerHomeFragment());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home2) {
                replaceFragment(new employerHomeFragment());

            } else if (item.getItemId() == R.id.notifications2) {
                replaceFragment(new employerNotiFragment());
            }
         else if (item.getItemId() == R.id.profile2) {
            replaceFragment(new employerprofFragment());
        }
            return true;
        });
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout2, fragment);
        fragmentTransaction.commit();
    }
    private void saveFcmTokenToDatabase(String userId, String fcmToken) {
        if (fcmToken != null) {
            databaseReference.child(userId).child("fcmToken").setValue(fcmToken)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FCM", "FCM token saved successfully!");
                        } else {
                            Log.w("FCM", "Failed to save FCM token", task.getException());
                        }
                    });
        }
    }
}


