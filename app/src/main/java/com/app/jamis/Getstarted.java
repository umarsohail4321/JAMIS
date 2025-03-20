package com.app.jamis;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class Getstarted extends AppCompatActivity {

    private Button showFragmentButton;
    private TextView textView;
    Animation fadeIn;
    private LinearLayout linearLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.getstarted);
        getWindow().setStatusBarColor(ContextCompat.getColor(Getstarted.this,R.color.grey));
        textView = findViewById(R.id.textView);
        linearLayout = findViewById(R.id.layout1);
        fadeIn = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        linearLayout.setAnimation(fadeIn);

        showFragmentButton = findViewById(R.id.showFragmentButton);

        showFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the button and replace it with a fragment
                showFragmentButton.setVisibility(View.GONE);

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ButtonFragment buttonFragment = new ButtonFragment();
                fragmentTransaction.replace(R.id.fragment_container, buttonFragment);
                fragmentTransaction.commit();
            }
        });
    }
}
