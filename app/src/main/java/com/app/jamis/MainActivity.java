package com.app.jamis;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity {
    private static int SPLASH_SCREEN = 5000;
    Animation topanim, bottomanim;
    ImageView imgvview;
    TextView slogan;
    String userType = "";
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        topanim = AnimationUtils.loadAnimation(this,R.anim.top);
        bottomanim = AnimationUtils.loadAnimation(this,R.anim.bottom);
        imgvview = findViewById(R.id.imageView2);
        slogan = findViewById(R.id.textView3);
        imgvview.setAnimation(topanim);
        slogan.setAnimation(bottomanim);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){

            if (!user.isEmailVerified()){
                Toast.makeText(this, "Please verify your email first, check your inbox", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, Getstarted.class));
                finish();
            }

            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                userType = snapshot.child("type").getValue(String.class);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (userType.isEmpty()){
                Intent intent = new Intent(MainActivity.this, EmployeeActivity.class);
                startActivity(intent);
                finish();
                }else{
                    Intent intent;
                    if (userType.equalsIgnoreCase("Employee")){
                        intent = new Intent(MainActivity.this, EmployeeActivity.class);
                    }else{
                        intent = new Intent(MainActivity.this, EmployerActivity.class);
                    }
                    startActivity(intent);
                    finish();
                }
            }
        },SPLASH_SCREEN);
    }
}