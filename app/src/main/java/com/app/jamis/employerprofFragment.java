package com.app.jamis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.app.jamis.databinding.FragmentEmployerprofBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class employerprofFragment extends Fragment {

    FragmentEmployerprofBinding binding;

    private DatabaseReference databaseReference;

    private ActivityResultLauncher<String> photoPickerLauncher;
    boolean isEditView = false;
    boolean isImage = false;
    Uri uri = null;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEmployerprofBinding.inflate(inflater, container, false);


        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        loadProfileData();

        Utils.checkAndRequestStoragePermission(requireActivity());

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(requireActivity(), MainActivity.class));
                            requireActivity().finish();
                            dialog.dismiss();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });

        binding.editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditView = true;
                binding.profileLayout.setVisibility(View.GONE);
                binding.editLayout.setVisibility(View.VISIBLE);
            }
        });


        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isEditView){
                    isEditView = false;
                    binding.editLayout.setVisibility(View.GONE);
                    binding.profileLayout.setVisibility(View.VISIBLE);
                }else{
                if (isEnabled()) {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }}
            }
        });


        // Initialize the ActivityResultLauncher inside onViewCreated
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            binding.profileImage.setImageURI(result);
                        }
                    }
                });

        // Handle photo upload using Android Photo Picker (Android 13+)
        binding.uploadPhotoBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Launch photo picker for Android 13+
                photoPickerLauncher.launch("image/*");
            } else {
                // Fallback for below Android 13 (pre-TIRAMISU)
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 200);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == getActivity().RESULT_OK) {
            Uri imageUri = data.getData();
            binding.profileImage.setImageURI(imageUri);
            uri = imageUri;
            isImage = true;
        }else{
            uri = null;
            isImage = false;
        }
    }

    private void loadProfileData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String designation = snapshot.child("designation").getValue(String.class);
                    String about = snapshot.child("about").getValue(String.class);
                    String company = snapshot.child("company").getValue(String.class);
                    String image = snapshot.child("image").getValue(String.class);

                    // Update UI with data and null checks
                    if (!TextUtils.isEmpty(name)) binding.nameInput.setText(name);
                    if (!TextUtils.isEmpty(email)) binding.emailInput.setText(email);
                    if (!TextUtils.isEmpty(designation)) binding.designationInput.setText(designation);
                    if (!TextUtils.isEmpty(about)) binding.aboutInput.setText(about);
                    if (!TextUtils.isEmpty(company)) binding.companyInput.setText(company);
                    if (!TextUtils.isEmpty(image)) Glide.with(requireContext()).load(image).into(binding.profileImage);


                    binding.employerName.setText(!TextUtils.isEmpty(name) ? name : "N/A");
                    binding.employerEmail.setText(!TextUtils.isEmpty(email) ? email : "N/A");
                    binding.employerDesignation.setText(!TextUtils.isEmpty(designation) ? designation : "N/A");
                    binding.companyProfile.setText(!TextUtils.isEmpty(about) ? about : "N/A");
                    binding.companyName.setText(!TextUtils.isEmpty(company) ? company : "N/A");
                    if (!TextUtils.isEmpty(image)) Glide.with(requireContext()).load(image).into(binding.profileImage2);

                } else {
                    Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileData() {
        String name = binding.nameInput.getText().toString().trim();
        String designation = binding.designationInput.getText().toString().trim();
        String about = binding.aboutInput.getText().toString().trim();
        String company = binding.companyInput.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), "Please fill name", Toast.LENGTH_SHORT).show();
            return;
        }



        databaseReference.child("fullName").setValue(name);
        databaseReference.child("designation").setValue(designation);
        databaseReference.child("about").setValue(about);
        if (isImage){
            Utils.uploadImage(requireContext(),uri,databaseReference.child("image"));
        }
        databaseReference.child("company").setValue(company).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }





}

